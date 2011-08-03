SRC=$(wildcard *.java)
CLS=$(SRC:.java=.class)
JCONFIG=-encoding UTF-8 -g:none
JAR=AsiWrapper.jar
REVFILE=REV-`svnversion`
RELEASE=asiwrapper.tar.gz
TMPDIR=asiwrapper

all: $(SRC)
	@echo -n Compiling…
	@javac $(JCONFIG) $?
	@echo " done."

.PHONY: clean jar release

jar: $(CLS)
	@echo -n Creating $(JAR)…
	@touch $(REVFILE)
	@jar cvfe $(JAR) Main *.class $(REVFILE) > /dev/null
	@rm $(REVFILE)
	@mv $(JAR) ../
	@cp config/* ../
	@echo " done."

clean:
	@echo -n Cleaning…
	@rm *.class > /dev/null 2>&1 || true
	@echo " done."

release: clean all jar
	@echo -n Creating package $(RELEASE)…
	@mkdir $(TMPDIR)
	@cp ../$(JAR) $(TMPDIR)/
	@cp config/* $(TMPDIR)/
	@tar czf $(RELEASE) $(TMPDIR)
	@rm -Rf $(TMPDIR)
	@echo " done."

