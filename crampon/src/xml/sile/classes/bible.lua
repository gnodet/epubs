local plain = SILE.require("classes/plain");
local bible = plain { id = "bible", base = plain };
if not(SILE.scratch.headers) then SILE.scratch.headers = {}; end

SILE.scratch.counters.page = { value= 1, display= "arabic" };
SILE.scratch.book = ""

bible:declareOption("twocolumns", "false")
bible:declareOption("gutter", "4mm")
bible:declareOption("inner", "22mm")
bible:declareOption("outer", "10mm")

SILE.scratch.masters = {}
local _currentMaster

local function defineMaster (self, args)
  SU.required(args, "id", "defining master")
  SU.required(args, "frames", "defining master")
  SU.required(args, "firstContentFrame", "defining master")
  SILE.scratch.masters[args.id] = {frames = {}, firstContentFrame = nil}
  for k,spec in pairs(args.frames) do
    spec.id=k
    if spec.solve then
      SILE.scratch.masters[args.id].frames[k] = spec
    else
      SILE.scratch.masters[args.id].frames[k] = SILE.newFrame(spec)
    end
  end
  SILE.frames = {page = SILE.frames.page}

  SILE.scratch.masters[args.id].firstContentFrame = SILE.scratch.masters[args.id].frames[args.firstContentFrame]
end

local function switchMaster (id)
  _currentMaster = id
  if not SILE.scratch.masters[id] then
    SU.error("Can't find master "..id)
  end
  SILE.documentState.documentClass.pageTemplate = SILE.scratch.masters[id]
  SILE.documentState.thisPageTemplate = std.tree.clone(SILE.documentState.documentClass.pageTemplate)
  doswitch(SILE.scratch.masters[id].frames)
  SILE.typesetter:initFrame(SILE.scratch.masters[id].firstContentFrame)
end

function bible:twoColumnMaster()
  local width = SILE.toPoints("100%pw")
  local gutterWidth = SILE.toPoints(self.options.gutter())
  local innerWidth = SILE.toPoints(self.options.inner())
  local outerWidth = SILE.toPoints(self.options.outer())
  local twocols = not(tostring(self.options.twocolumns())=="false")
  local usebalancing = false
  SILE.scratch.headWidth = width - innerWidth - outerWidth

	  self:defineMaster({ id = "title", firstContentFrame = "contentA", frames = {
	    title = {left = "left(contentA)", right = "right(contentB)", top="1cm", bottom="10.5cm" },
	    contentA = {left = innerWidth, right = "left(gutter)", top = "bottom(title)", bottom = "top(footnotesA)", next = "contentB", balanced = usebalancing },
	    contentB = {left = "right(gutter)", width="width(contentA)", right = width - outerWidth, top = "bottom(title)", bottom = "top(footnotesB)", balanced = usebalancing },
	    gutter = { left = "right(contentA)", right = "left(contentB)", width = gutterWidth },
	    folio = { left = "150%pw", right="200%pw", top = "0", height = "0" },
	    runningHead = {left = "left(contentA)", right = "right(contentB)", top = "0.5cm", bottom = "1cm" },
	    footnotesA = { left="left(contentA)", right = "right(contentA)", height = "0", bottom="95%ph"},
	    footnotesB = { left="left(contentB)", right = "right(contentB)", height = "0", bottom="95%ph"},
	  }})
	  self:defineMaster({ id = "right", firstContentFrame = "contentA", frames = {
	    title = {left = "left(contentA)", right = "right(contentB)", top="1cm", height="0", bottom="top(contentA)" },
	    contentA = {left = innerWidth, right = "left(gutter)", top = "bottom(title)", bottom = "top(footnotesA)", next = "contentB", balanced = usebalancing },
	    contentB = {left = "right(gutter)", width="width(contentA)", right = width - outerWidth, top = "bottom(title)", bottom = "top(footnotesB)", balanced = usebalancing },
	    gutter = { left = "right(contentA)", right = "left(contentB)", width = gutterWidth },
	    folio = { left = "150%pw", right="200%pw", top = "0", height = "0" },
	    runningHead = {left = "left(contentA)", right = "right(contentB)", top = "0.5cm", bottom = "1cm" },
	    footnotesA = { left="left(contentA)", right = "right(contentA)", height = "0", bottom="95%ph"},
	    footnotesB = { left="left(contentB)", right = "right(contentB)", height = "0", bottom="95%ph"},
	  }})
	  self:defineMaster({ id = "left", firstContentFrame = "contentA", frames = {
	    title = {left = "left(contentA)", right = "right(contentB)", top="1cm", height="0", bottom="top(contentA)" },
	    contentA = {left = outerWidth, right = "left(gutter)", top = "bottom(title)", bottom = "top(footnotesA)", next = "contentB", balanced = usebalancing },
	    contentB = {left = "right(gutter)", width="width(contentA)", right = width - innerWidth, top = "bottom(title)", bottom = "top(footnotesB)", balanced = usebalancing },
	    gutter = { left = "right(contentA)", right = "left(contentB)", width = gutterWidth },
	    folio = { left = "150%pw", right="200%pw", top = "0", height = "0" },
	    runningHead = {left = "left(contentA)", right = "right(contentB)", top = "0.5cm", bottom = "1cm" },
	    footnotesA = { left="left(contentA)", right = "right(contentA)", height = "0", bottom="95%ph"},
	    footnotesB = { left="left(contentB)", right = "right(contentB)", height = "0", bottom="95%ph"},
	  }})
	  self:defineMaster({ id = "toc-right", firstContentFrame = "content", frames = {
	    title = {left = "left(content)", right = "right(content)", top="1cm", height="0" },
	    content = {left = innerWidth, right = width - outerWidth, top = "bottom(title)", bottom = "top(footnotes)", balanced = false },
	    folio = { left = "150%pw", right="200%pw", top = "0", height = "0" },
	    runningHead = {left = "left(content)", right = "right(content)", top = "0.5cm", bottom = "1cm" },
	    footnotes = { left="left(content)", right = "right(content)", height = "0", bottom="95%ph"},
	  }})
	  self:defineMaster({ id = "toc-left", firstContentFrame = "content", frames = {
	    title = {left = "left(content)", right = "right(content)", top="1cm", height="0" },
	    content = {left = outerWidth, right = width - innerWidth, top = "bottom(title)", bottom = "top(footnotes)", balanced = false },
	    folio = { left = "150%pw", right="200%pw", top = "0", height = "0" },
	    runningHead = {left = "left(content)", right = "right(content)", top = "0.5cm", bottom = "1cm" },
	    footnotes = { left="left(content)", right = "right(content)", height = "0", bottom="95%ph"},
	  }})
	  -- Later we'll have an option for two fn frames
	  self:loadPackage("footnotes", { insertInto = "footnotesB", stealFrom = {"contentB"} } )
	  if usebalancing then
		  self:loadPackage("balanced-frames")
	  end
end

function bible:init()
  self:loadPackage("rules")
  self:loadPackage("masters")
  self:loadPackage("infonode")
  self:loadPackage("chapterverse")
  self:loadPackage("tableofcontents")
  self:twoColumnMaster()
  SILE.settings.set("linebreak.tolerance", 9000)
  self.pageTemplate = SILE.scratch.masters["right"]
  SILE.call("nofolios")
  return plain.init(self)
end

local doTitlePage = false
local doToc = false
local tp = "odd"

bible.oddPage = function(self) 
	return tp == "odd" 
end

function bible:tocMaster(o, c)
	-- local oldOdd = self.oddPageMaster
	-- local oldEven = self.evenPageMaster
	-- self.evenPageMaster = "toc-left"
	-- self.oddPageMaster = "toc-right"
	-- SILE.call("open-double-page")
	doToc = true
	SILE.call("open-double-page")
	SILE.process(c)
	doToc = false
	-- self.evenPageMaster = oldEven
	-- self.oddPageMaster = oldOdd
end

function bible:switchPage()
    if self.oddPage() then
      tp = "even"
      if doToc then
        SU.debug("page", "Switching to toc-left")
        self.switchMaster("toc-left")
      else
        SU.debug("page", "Switching to left")
        self.switchMaster("left")
      end
    elseif doTitlePage then
      doTitlePage = false
      tp = "odd"
      SU.debug("page", "Switching to title")
      self.switchMaster("title")
    else
      tp = "odd"
      if doToc then
        SU.debug("page", "Switching to toc-right")
        self.switchMaster("toc-right")
      else
        SU.debug("page", "Switching to right")
        self.switchMaster("right")
      end
    end
end

bible.newPage = function(self)
  self:switchPage()
  self:newPageInfo()
  local r = plain.newPage(self)
  -- SILE.typesetNaturally(SILE.getFrame("contentA"), function()
    -- SILE.call("showframe", {id="all"})
  -- end)
  -- SILE.typesetNaturally(SILE.getFrame("contentB"), function()
    -- SILE.call("grid:debug")
  -- end)
  -- SU.debug("grid", "newPage")
  return r
end

bible.newPar = function(typesetter)
    typesetter.typeset = function (self, text)
        -- SU.debug("test", "typeset: "..text)
		if (text == nil) then
			print("self: "..self..", text: "..text)
			print(debug.traceback())
		end
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

 bible.endPar = function(self)
-- 	  return plain.endPar(self)
 end

bible.finish = function (self)
  local r = plain.finish(self)
  bible:writeToc()
  return r
end

bible.endPage = function(self)
  bible:moveTocNodes()
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

SILE.registerCommand("book-title", function (o,c)
  SU.debug("page", "book-title: "..c)
  SILE.call("open-double-page")
  SILE.call("switch-master-one-page", { id = "title" }, {})
  SILE.call("typeset-into", {frame = "title"}, function() 
	  SILE.call("bible:book-title", o, c)
	  end)
  SILE.call("save-book-title", o, c)
  SILE.call("tocentry", o, c)
  SU.debug("page", "book-title: processed "..c)
end)

SILE.registerCommand("chapter", function (o,c)
  local ch = o.id:match("%d+")
  SILE.call("bible:chapter-head", o, {"Chapter "..ch})
  SILE.call("save-chapter-number", o, {o.id})
  SILE.process(c)
end)

SILE.registerCommand("verse-number", function (o,c)
  SU.debug("bible", "verse-number: "..c)
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
      SILE.call("hrule", {width=SILE.scratch.headWidth, height="0.3pt"}, {})
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
      SILE.call("hrule", {width=SILE.scratch.headWidth, height="0.3pt"}, {})
    end)
  end)
end)

SILE.registerCommand("toc-master", function(o, c)
	SILE.documentState.documentClass:tocMaster(o, c)
end)

SILE.registerCommand("open-double-page", function()
  SILE.typesetter:leaveHmode();
  SILE.Commands["supereject"]();
  if SILE.documentState.documentClass:oddPage() then
    SILE.typesetter:typeset("")
    SILE.typesetter:leaveHmode();
    SILE.Commands["supereject"]();
  end
  SILE.typesetter:leaveHmode();
end)

SILE.registerCommand("indent", function ( options, content )
  SILE.process(content)
end, "Do add an indent to the start of this paragraph, even if previously told otherwise")

SILE.settings.set("document.parindent", SILE.nodefactory.newGlue("0pt"))
return bible
