SRC=$(wildcard *.java)
CLS=$(SRC:.java=.class)
JAR=AsiWrapper.jar
REVFILE=REV-`svnversion`

all: $(SRC)
	@javac $?

.PHONY: clean jar 

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

