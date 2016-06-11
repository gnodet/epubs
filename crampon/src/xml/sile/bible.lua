SILE.registerCommand("verse", function(options, content)
  local c = options.ch
  local v = options.vs
  SILE.call("break")
  SILE.scratch.isBreaking = false
  if (tonumber(c) > 2) then return end
  if (v == "1") then
    io.write(" ("..c..") ")
    SU.debug("test", "Calling chapter: "..c)
	SILE.call("par")
    SILE.call("chapter-number", o, {c})
    SILE.call("save-chapter-number",{},{c})
  else
    SILE.process({" "})
  end
  SU.debug("test", "Processing: "..c..","..v)
  SILE.call("verse-number",{},{v})
  if (v == "1" and c == "1") then
    SILE.call("tocentry",{},{SILE.scratch.book})
  end
  SILE.process(content)
  -- SILE.call("grid:debug")
  -- SILE.typesetNaturally(SILE.getFrame("contentA"), function()
  --   SILE.call("grid:debug")
  -- end)
  -- SILE.typesetNaturally(SILE.getFrame("contentB"), function()
  --   SILE.call("grid:debug")
  -- end)
end)

SILE.registerCommand("book", function(options, content)
  local node = SILE.findInTree(content, "title") or SILE.findInTree(content, "h2")
  local title
  SU.debug("test", "node: "..node)
  for _,t in ipairs(node) do
    SU.debug("test", t.."("..type(t)..")")
    if type(t) == "string" then
      title = t
    end
  end
  SU.debug("test", "title: "..title)
  SILE.scratch.book=title
  SILE.call("book-title", {}, {title})
  SILE.process(content)
end)

SILE.registerCommand("break", function(options, content)
end)

SILE.registerCommand("section", function(options, content)
  if not(options.type == "intro") then
    SILE.process(content)
  end
end)

SILE.registerCommand("p", function(options, content)
  if not(options.type == "intro") then
    SILE.process(content)
  end
end)

SILE.registerCommand("format-reference", function (o,c)
  SU.debug("bcv", "formatting: "..c)
  local ref
  if c.b then
	ref =  c.b .. " " .. c.c .. ", " .. c.v
  else
  	ref =  c.c .. ", " .. c.v
  end
  SU.debug("bcv", "formatting: "..ref)
  SILE.typesetter:typeset(ref)
end)
