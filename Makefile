ARGS = test

all: compile run

compile:
	cd src && \
	javac -d ../bin Main.java

run:
	cd bin && \
	java Main $(ARGS)
