#!/usr/bin/env python3
"""
Convert a modern Vatican.va HTML encyclical to project docbook XML.

Usage:
    python3 convert_vatican.py <html_file> <xml_id> <title> <subtitle> <author>

Output: XML to stdout, log to stderr.
Redirect correctly: python3 convert_vatican.py ... > out.xml 2>/tmp/log.txt

DO NOT use 2>&1 -- Python warnings land in the XML and corrupt it.

Supports:
  - Modern Vatican.va pages (François, Léon XIV, etc.): content in .testo/.documento
  - Duplicated TOC: skips to second INTRODUCTION automatically
  - Numbered paragraphs: "N. text" pattern
  - Chapter titles, section subtitles, inline markup (i, b, sup, a, br)

For Ictus HTML4 format (old cédérom files), use convert_ictus.py instead.
"""
import sys
import re
from lxml import html as lhtml
from xml.etree import ElementTree as ET


def norm(txt):
    return re.sub(r'\s+', ' ', txt.replace('\xa0', ' ')).strip()


def esc(s):
    if not s:
        return ''
    return (s.replace('&', '&amp;')
             .replace('<', '&lt;')
             .replace('>', '&gt;')
             .replace('"', '&quot;'))


def inner_xml(p):
    """Extract inline content of a <p>, normalizing whitespace at each text node."""
    parts = []

    def add_text(t):
        if t:
            t = t.replace('\xa0', ' ')
            t = re.sub(r'\s+', ' ', t)
            parts.append(esc(t))

    add_text(p.text)
    for child in p:
        tag = child.tag if isinstance(child.tag, str) else None
        if tag is None:
            add_text(child.tail)
            continue
        ci = re.sub(r'\s+', ' ', (''.join(child.itertext() or [])).replace('\xa0', ' ')).strip()
        if tag in ('i', 'em'):
            parts.append(f'<i>{esc(ci)}</i>')
        elif tag in ('b', 'strong'):
            parts.append(f'<b>{esc(ci)}</b>')
        elif tag == 'sup' and ci:
            parts.append(f'<sup>{esc(ci)}</sup>')
        elif tag == 'a':
            href = child.get('href', '')
            if href and ci:
                parts.append(f'<a href="{esc(href)}">{esc(ci)}</a>')
            else:
                add_text(ci)
        elif tag == 'br':
            parts.append('<br/>')
        else:
            add_text(ci)
        add_text(child.tail)
    return re.sub(r'  +', ' ', ''.join(parts)).strip()


def convert(html_path, xml_id, title, subtitle, author):
    with open(html_path, 'rb') as f:
        raw = f.read()
    tree = lhtml.fromstring(raw)

    # Use explicit 'is not None' -- never 'or' with lxml elements (triggers FutureWarning to stdout)
    testo = tree.find('.//*[@class="testo"]')
    if testo is None:
        testo = tree.find('.//*[@class="documento"]')
    all_p = testo.findall('.//p') if testo is not None else tree.findall('.//p')

    # Skip duplicated TOC: find second INTRODUCTION occurrence
    intro_indices = [
        i for i, p in enumerate(all_p)
        if norm(''.join(p.itertext())) in ('INTRODUCTION', 'Introduction')
    ]
    start = intro_indices[1] if len(intro_indices) > 1 else 0
    content_ps = all_p[start:]
    print(f'Content paragraphs: {len(content_ps)} (start at idx {start})', file=sys.stderr)

    def is_chapter(txt):
        return bool(re.match(
            r'^(Chapitre \d+|INTRODUCTION|Introduction|CONCLUSION|Conclusion|PRÉAMBULE|Préambule)$',
            txt
        ))

    def is_skip(txt):
        return (not txt
                or txt in ('[Multimédia]', 'Multimédia')
                or re.match(r'^\[.*\]$', txt)
                or txt.startswith('___')
                or txt.startswith('----'))

    out = []
    out.append(f'''<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="preview.xsl"?>
<book xmlns="http://gnodet.fr/ns/docbook"
\t  xml:id="{xml_id}" xml:lang="fr">
\t<info>
\t\t<productnumber><?eval ${{project.version}}?></productnumber>
\t\t<title>{esc(title)}</title>
\t\t<subtitle>{esc(subtitle)}</subtitle>
\t\t<author>{esc(author)}</author>
\t\t<cover name="{xml_id}"/>
\t</info>
\t<chapter>
\t\t<title>{esc(title.upper())}</title>''')

    section_open = False
    in_footnotes = False

    def open_section(section_title):
        nonlocal section_open
        if section_open:
            out.append('\t\t</section>')
        out.append('\t\t<section>')
        out.append(f'\t\t\t<title>{esc(section_title)}</title>')
        section_open = True

    i = 0
    while i < len(content_ps):
        p = content_ps[i]
        txt = norm(''.join(p.itertext()))
        inner = inner_xml(p)

        if in_footnotes:
            i += 1
            continue

        if is_skip(txt):
            if txt.startswith('___') or txt.startswith('----'):
                in_footnotes = True
            i += 1
            continue

        if is_chapter(txt):
            next_title = None
            if i + 1 < len(content_ps):
                ntxt = norm(''.join(content_ps[i + 1].itertext()))
                if ntxt and len(ntxt) > 5 and not re.match(r'^\d+\.', ntxt) and not is_chapter(ntxt):
                    next_title = ntxt
                    i += 1
            label = txt + (f' \u2014 {next_title}' if next_title else '')
            open_section(label)
            i += 1
            continue

        m = re.match(r'^(\d+)\.\s+(.*)', txt, re.DOTALL)
        if m:
            num = m.group(1)
            body = re.sub(r'^' + re.escape(num) + r'\.\s*', '', inner, count=1)
            out.append(f'\t\t\t<p><np>{num}</np> {body}</p>')
            i += 1
            continue

        # Short non-numbered paragraph = section subtitle
        if txt and 3 < len(txt) < 150 and not re.match(r'^\d+', txt):
            out.append('\t\t\t<section>')
            out.append(f'\t\t\t\t<title>{esc(txt)}</title>')
            out.append('\t\t\t</section>')
            i += 1
            continue

        if txt:
            out.append(f'\t\t\t<p>{inner}</p>')
        i += 1

    if section_open:
        out.append('\t\t</section>')
    out.append('\t</chapter>')
    out.append('</book>')

    result = '\n'.join(out)

    try:
        ET.fromstring(result)
        valid = True
    except ET.ParseError as e:
        valid = False
        print(f'XML ERROR: {e}', file=sys.stderr)

    np_count = len(re.findall('<np>', result))
    cuts = re.findall(r"'[a-zA-Z\u00C0-\u00FF] [a-z\u00E0-\u00FF]", result)
    print(f'\u00a7\u00a7: {np_count}, valid: {valid}, intra-word cuts: {len(cuts)}', file=sys.stderr)
    return result


if __name__ == '__main__':
    html_path, xml_id, title, subtitle, author = sys.argv[1:]
    sys.stdout.write(convert(html_path, xml_id, title, subtitle, author))
