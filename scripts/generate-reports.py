#!/usr/bin/env python3
"""
Generate epubcheck HTML reports for all EPUBs in a site directory.

Produces:
  target/site/reports/report.html          — aggregated table (all epubs)
  target/site/reports/<slug>-report.html   — individual report per epub

Usage:
  python3 scripts/generate-reports.py <site_dir> <epubcheck_jar> <classpath>

  <site_dir>      path to target/site/ (must contain an epub/ subdirectory)
  <epubcheck_jar> not used directly — pass full classpath in <classpath>
  <classpath>     colon-separated jar list for the epubcheck CLI
"""

import json
import os
import subprocess
import sys
from html import escape
from pathlib import Path

TEMPLATE = """\
<!DOCTYPE html>
<html>
<head>
    <meta charset='utf-8' />
    <meta http-equiv="X-UA-Compatible" content="chrome=1" />
    <meta name="description" content="Epubs : " />
    <link rel="stylesheet" type="text/css" media="screen" href="../stylesheets/stylesheet.css">
    <script type="text/javascript" src="../readium-js-viewer/lib/thirdparty/jquery-1.11.0.js"></script>
    <script type="text/javascript" src="../javascripts/main.js"></script>
    <script type="text/javascript">
        var _gaq = _gaq || [];
        _gaq.push(['_setAccount', 'UA-52204827-1']);
        _gaq.push(['_trackPageview']);
        (function() {
            var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
            ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
            var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
        })();
    </script>
    <title>%(TITLE)s</title>
</head>
<body>
<!-- HEADER -->
<div id="header_wrap" class="outer">
    <header class="inner">
        <a id="forkme_banner" href="https://github.com/gnodet/epubs">View on GitHub</a>
        <h1 id="project_title">Epubs</h1>
        <h2 id="project_tagline"></h2>
        <section id="downloads">
            <a class="zip_download_link" href="../archives/epubs.zip">Téléchargez les epubs dans un fichier .zip</a>
        </section>
    </header>
</div>
<!-- MAIN CONTENT -->
<div id="main_content_wrap" class="outer">
    <section id="main_content" class="inner">
        %(CONTENT)s
    </section>
</div>
<!-- FOOTER  -->
<div id="footer_wrap" class="outer">
    <footer class="inner">
        <p class="copyright">Le projet Epubs est maintenu par <a href="mailto:gnodet@gmail.com">Guillaume Nodet</a>.</p>
    </footer>
</div>
%(SCRIPT)s
</body>
</html>
"""

INDIVIDUAL_SCRIPT = """\
<script type="text/javascript">
var report = %(REPORT)s;
exports = {};
exports.renderjson = renderjson = (function() {
    var themetext = function() {
        var spans = [];
        while (arguments.length)
            spans.push(append(span(Array.prototype.shift.call(arguments)),
                              text(Array.prototype.shift.call(arguments))));
        return spans;
    };
    var append = function() {
        var el = Array.prototype.shift.call(arguments);
        for (var a=0; a<arguments.length; a++)
            if (arguments[a].constructor == Array)
                append.apply(this, [el].concat(arguments[a]));
            else
                el.appendChild(arguments[a]);
        return el;
    };
    var prepend = function(el, child) { el.insertBefore(child, el.firstChild); return el; };
    var isempty = function(obj) { for (var k in obj) if (obj.hasOwnProperty(k)) return false; return true; };
    var text = function(txt) { return document.createTextNode(txt) };
    var div = function() { return document.createElement("div") };
    var span = function(classname) { var s = document.createElement("span");
                                     if (classname) s.className = classname;
                                     return s; };
    var A = function A(txt, classname, callback) { var a = document.createElement("a");
                                                   if (classname) a.className = classname;
                                                   a.appendChild(text(txt));
                                                   a.href = '#';
                                                   a.onclick = function() { callback(); return false; };
                                                   return a; };
    function _renderjson(json, indent, dont_indent, show_level) {
        var my_indent = dont_indent ? "" : indent;
        if (json === null) return themetext(null, my_indent, "keyword", "null");
        if (json === void 0) return themetext(null, my_indent, "keyword", "undefined");
        if (typeof(json) != "object")
            return themetext(null, my_indent, typeof(json), JSON.stringify(json));
        var disclosure = function(open, close, type, builder) {
            var content;
            var empty = span(type);
            var show = function() { if (!content) append(empty.parentNode,
                                                         content = prepend(builder(),
                                                                           A(renderjson.hide, "disclosure",
                                                                             function() { content.style.display="none";
                                                                                          empty.style.display="inline"; })));
                                    content.style.display="inline";
                                    empty.style.display="none"; };
            append(empty,
                   A(renderjson.show, "disclosure", show),
                   themetext(type+" syntax", open),
                   A(" ... ", null, show),
                   themetext(type+" syntax", close));
            var el = append(span(), text(my_indent.slice(0,-1)), empty);
            if (show_level > 0) show();
            return el;
        };
        if (json.constructor == Array) {
            if (json.length == 0) return themetext(null, my_indent, "array syntax", "[]");
            return disclosure("[", "]", "array", function () {
                var as = append(span("array"), themetext("array syntax", "[", null, "\\n"));
                for (var i=0; i<json.length; i++)
                    append(as,
                           _renderjson(json[i], indent+"    ", false, show_level-1),
                           i != json.length-1 ? themetext("syntax", ",") : [],
                           text("\\n"));
                append(as, themetext(null, indent, "array syntax", "]"));
                return as;
            });
        }
        if (isempty(json)) return themetext(null, my_indent, "object syntax", "{}");
        return disclosure("{", "}", "object", function () {
            var os = append(span("object"), themetext("object syntax", "{", null, "\\n"));
            for (var k in json) var last = k;
            for (var k in json)
                append(os, themetext(null, indent+"    ", "key", '"'+k+'"', "object syntax", ': '),
                       _renderjson(json[k], indent+"    ", true, show_level-1),
                       k != last ? themetext("syntax", ",") : [],
                       text("\\n"));
            append(os, themetext(null, indent, "object syntax", "}"));
            return os;
        });
    }
    var renderjson = function renderjson(json) {
        var pre = append(document.createElement("pre"), _renderjson(json, "", false, renderjson.show_to_level));
        pre.className = "renderjson";
        return pre;
    };
    renderjson.set_icons = function(show, hide) { renderjson.show = show; renderjson.hide = hide; return renderjson; };
    renderjson.set_show_to_level = function(level) {
        renderjson.show_to_level = typeof level == "string" && level.toLowerCase() === "all" ? Number.MAX_VALUE : level;
        return renderjson;
    };
    renderjson.set_show_by_default = function(show) { renderjson.show_to_level = show ? Number.MAX_VALUE : 0; return renderjson; };
    renderjson.set_icons('\\u2295', '\\u2296');
    renderjson.set_show_by_default(false);
    return renderjson;
})();
document.getElementById("divreport").appendChild(renderjson(report));
</script>
"""


def run_epubcheck(epub_path: Path, classpath: str, java: str) -> dict:
    """Run epubcheck and return parsed JSON result."""
    json_out = epub_path.with_suffix(".json")
    try:
        subprocess.run(
            [java, "-cp", classpath, "com.adobe.epubcheck.tool.Checker",
             str(epub_path), "--json", str(json_out)],
            capture_output=True, timeout=120
        )
        if json_out.exists():
            with open(json_out, encoding="utf-8") as f:
                return json.load(f)
    finally:
        if json_out.exists():
            json_out.unlink()
    return {}


def make_individual_report(slug: str, data: dict, reports_dir: Path) -> None:
    """Generate reports/<slug>-report.html from epubcheck JSON."""
    checker = data.get("checker", {})
    filename = checker.get("filename", slug + ".epub")
    n_fatal = checker.get("nFatal", 0)
    n_error = checker.get("nError", 0)
    n_warning = checker.get("nWarning", 0)

    if n_fatal == 0 and n_error == 0:
        status_html = "<b>Valide</b>"
        if n_warning:
            status_html += f" ({n_warning} avertissement{'s' if n_warning > 1 else ''})"
    else:
        parts = []
        if n_fatal:
            parts.append(f"{n_fatal} fatal{'s' if n_fatal > 1 else ''}")
        if n_error:
            parts.append(f"{n_error} erreur{'s' if n_error > 1 else ''}")
        status_html = "<b>Invalide</b>(" + ", ".join(parts) + ")"

    # Messages table
    messages = data.get("messages", [])
    if messages:
        rows = []
        for msg in messages:
            level = escape(msg.get("ID", "").split("-")[0] if msg.get("ID") else msg.get("severity", ""))
            text = escape(msg.get("message", ""))
            rows.append(f"<tr><td>{level}</td><td>{text}</td></tr>")
        msg_html = (
            "<h3>Messages</h3>"
            "<table><tr><th>Niveau</th><th>Message</th></tr>"
            + "".join(rows) + "</table>"
        )
    else:
        msg_html = ""

    content = (
        f"<h2>Rapport EpubCheck&nbsp;: {escape(filename)}</h2>"
        f"<h3>Résumé</h3>"
        f"<ul><li><b>Statut</b>&nbsp;: {status_html}</li></ul>"
        f"{msg_html}"
        f"<h3>Rapport complet</h3>"
        f'<div id="divreport"></div>'
    )

    script = INDIVIDUAL_SCRIPT % {"REPORT": json.dumps(data, ensure_ascii=False)}

    html = TEMPLATE % {
        "TITLE": f"EpubCheck Report for {escape(filename)}",
        "CONTENT": content,
        "SCRIPT": script,
    }

    out_path = reports_dir / f"{slug}-report.html"
    out_path.write_text(html, encoding="utf-8")


def make_aggregate_report(results: list, reports_dir: Path) -> None:
    """Generate reports/report.html aggregate table."""
    rows = []
    for slug, data in sorted(results, key=lambda x: x[0]):
        checker = data.get("checker", {})
        filename = checker.get("filename", slug + ".epub")
        n_fatal = checker.get("nFatal", 0)
        n_error = checker.get("nError", 0)
        n_warning = checker.get("nWarning", 0)
        status = "Valide" if (n_fatal == 0 and n_error == 0) else "Invalide"
        rows.append(
            f'<tr><td><a href="{slug}-report.html">{escape(filename)}</a></td>'
            f"<td>{status}</td>"
            f"<td>{n_fatal}</td><td>{n_error}</td><td>{n_warning}</td></tr>"
        )

    n_valid = sum(1 for _, d in results if d.get("checker", {}).get("nFatal", 0) == 0
                  and d.get("checker", {}).get("nError", 0) == 0)
    n_total = len(results)

    content = (
        f"<h2>Rapport Global EpubCheck</h2>"
        f"<p>{n_valid}/{n_total} epub(s) valides.</p>"
        "<table>"
        "<tr><th>EPub</th><th>Status</th><th>F</th><th>E</th><th>W</th></tr>"
        + "".join(rows)
        + "</table>"
    )

    html = TEMPLATE % {
        "TITLE": "Rapport Global EpubCheck",
        "CONTENT": content,
        "SCRIPT": "",
    }

    out_path = reports_dir / "report.html"
    out_path.write_text(html, encoding="utf-8")
    print(f"  → reports/report.html ({n_valid}/{n_total} valides)")


def main():
    if len(sys.argv) < 4:
        print("Usage: generate-reports.py <site_dir> <java> <classpath>")
        sys.exit(1)

    site_dir = Path(sys.argv[1])
    java = sys.argv[2]
    classpath = sys.argv[3]

    epub_dir = site_dir / "epub"
    reports_dir = site_dir / "reports"
    reports_dir.mkdir(exist_ok=True)

    epubs = sorted(epub_dir.glob("*.epub"))
    print(f"Found {len(epubs)} epub(s) in {epub_dir}")

    results = []
    for epub in epubs:
        slug = epub.stem
        print(f"  Checking {slug}...", end=" ", flush=True)
        data = run_epubcheck(epub, classpath, java)
        checker = data.get("checker", {})
        nf, ne, nw = checker.get("nFatal", 0), checker.get("nError", 0), checker.get("nWarning", 0)
        status = "OK" if (nf == 0 and ne == 0) else f"F={nf} E={ne} W={nw}"
        print(status)
        make_individual_report(slug, data, reports_dir)
        results.append((slug, data))

    make_aggregate_report(results, reports_dir)
    print(f"Done. Reports written to {reports_dir}")


if __name__ == "__main__":
    main()
