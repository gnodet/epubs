local plain = SILE.require("classes/plain");
local bible = plain { id = "bible", base = plain };
if not(SILE.scratch.headers) then SILE.scratch.headers = {}; end

SILE.scratch.counters.page = { value= 1, display= "arabic" };
SILE.scratch.book = ""

function bible:twoColumnMaster()
  local gutterWidth = self.options.gutter or "3%"
  self:defineMaster({ id = "right", firstContentFrame = "contentA", frames = {
    title = {left = "left(contentA)", right = "right(contentB)", top="1cm", height="0", bottom="top(contentA)" },
    contentA = {left = "8%", right = "left(gutter)", top = "bottom(title)", bottom = "top(footnotesA)", next = "contentB", balanced = false },
    contentB = {left = "right(gutter)", width="width(contentA)", right = "95%", top = "bottom(title)", bottom = "top(footnotesB)", balanced = true },
    gutter = { left = "right(contentA)", right = "left(contentB)", width = gutterWidth },
    runningHead = {left = "left(contentA)", right = "right(contentB)", top = "0.5cm", bottom = "1cm" },
    footnotesA = { left="left(contentA)", right = "right(contentA)", height = "0", bottom="95%"},
    footnotesB = { left="left(contentB)", right = "right(contentB)", height = "0", bottom="95%"},

  }})
  -- Later we'll have an option for two fn frames
  self:loadPackage("footnotes", { insertInto = "footnotesB", stealFrom = {"contentB"} } )
  self:loadPackage("balanced-frames")
end

function bible:init()
  self:loadPackage("rules")
  self:loadPackage("masters")
  self:loadPackage("infonode")
  self:loadPackage("chapterverse")
  self:twoColumnMaster()
  SILE.settings.set("linebreak.tolerance", 9000)
  self:loadPackage("twoside", { oddPageMaster = "right", evenPageMaster = "left" });
  self:mirrorMaster("right", "left")
  -- mirrorMaster is not clever enough to handle two-column layouts,
  -- and it mirrors them!
  SILE.scratch.masters.left.firstContentFrame = SILE.scratch.masters.left.frames.contentB
  SILE.scratch.masters.left.frames.contentB.next = "contentA"
  SILE.scratch.masters.left.frames.contentA.next = nil
  self.pageTemplate = SILE.scratch.masters["right"]
  SILE.call("nofolios")
  return plain.init(self)
end

bible.newPage = function(self)
  self:switchPage()
  self:newPageInfo()
  return plain.newPage(self)
end

bible.newPar = function(typesetter)
    typesetter.typeset = function (self, text)
        SU.debug("test", "typeset: "..text)
        text = string.gsub(text, "[\n\t ]+", " ")
        if text:match("^%\n$") then return end
        for t in SU.gtoke(text,SILE.settings.get("typesetter.parseppattern")) do
          if (t.separator) then
            self:leaveHmode();
            SILE.documentState.documentClass.endPar(self)
          else self:setpar(t.string)
          end
        end
      end
end

-- bible.endPar = function(self)
-- 	  return plain.endPar(self)
-- end

bible.finish = function (self)
  --bible:writeToc()
  return plain.finish(self)
end

bible.endPage = function(self)
  if (self:oddPage() and SILE.scratch.headers.right) then
    SILE.typesetNaturally(SILE.getFrame("runningHead"), function()
      SILE.settings.set("current.parindent", SILE.nodefactory.zeroGlue)
      SILE.settings.set("document.lskip", SILE.nodefactory.zeroGlue)
      SILE.settings.set("document.rskip", SILE.nodefactory.zeroGlue)
      -- SILE.settings.set("typesetter.parfillskip", SILE.nodefactory.zeroGlue)
      SILE.process(SILE.scratch.headers.right)
      SILE.call("par")
    end)
  elseif (not(self:oddPage()) and SILE.scratch.headers.left) then
      SILE.typesetNaturally(SILE.getFrame("runningHead"), function()
        SILE.settings.set("current.parindent", SILE.nodefactory.zeroGlue)
        SILE.settings.set("document.lskip", SILE.nodefactory.zeroGlue)
        SILE.settings.set("document.rskip", SILE.nodefactory.zeroGlue)
        -- SILE.settings.set("typesetter.parfillskip", SILE.nodefactory.zeroGlue)
        SILE.process(SILE.scratch.headers.left)
        SILE.call("par")
      end)
  end
  SILE.scratch.counters.page.value = SILE.scratch.counters.page.value + 1
  return plain.endPage(self);
end;


SILE.registerCommand("left-running-head", function(options, content)
  local closure = SILE.settings.wrap()
  SILE.scratch.headers.left = function () closure(content) end
end, "Text to appear on the top of the left page");

SILE.registerCommand("right-running-head", function(options, content)
  local closure = SILE.settings.wrap()
  SILE.scratch.headers.right = function () closure(content) end
end, "Text to appear on the top of the right page");


SILE.registerCommand("chapter", function (o,c)
  local ch = o.id:match("%d+")
  SILE.call("bible:chapter-head", o, {"Chapter "..ch})
  SILE.call("save-chapter-number", o, {o.id})
  SILE.process(c)
end)

SILE.registerCommand("verse-number", function (o,c)
  SILE.call("indent")
  SILE.call("bible:verse-number", o, c)
  SILE.call("save-verse-number", o, c)
  SILE.call("left-running-head", {}, function ()
    SILE.settings.temporarily(function()
      SILE.settings.set("document.lskip", SILE.nodefactory.zeroGlue)
      SILE.settings.set("document.rskip", SILE.nodefactory.zeroGlue)
      SILE.call("font", {size="10pt", family="Gentium"}, function ()
		  SILE.call("first-reference")
          SILE.call("hfill")
          SILE.typesetter:typeset(SILE.formatCounter(SILE.scratch.counters.page))
      end)
      SILE.typesetter:leaveHmode()
      SILE.call("hrule", {width="87%", height="0.3pt"}, {})
    end)
  end)
  SILE.call("right-running-head", {}, function ()
    SILE.settings.temporarily(function()
      SILE.settings.set("document.lskip", SILE.nodefactory.zeroGlue)
      SILE.settings.set("document.rskip", SILE.nodefactory.zeroGlue)
      SILE.settings.set("typesetter.parfillskip", SILE.nodefactory.zeroGlue)
      SILE.call("font", {size="10pt", family="Gentium"}, function ()
          SILE.typesetter:typeset(SILE.formatCounter(SILE.scratch.counters.page))
          SILE.call("hfill")
		  SILE.call("last-reference")
      end)
      SILE.typesetter:leaveHmode()
      SILE.call("hrule", {width="87%", height="0.3pt"}, {})
    end)
  end)
end)

return bible
