ARGS = test.qk

all: compile run

compile:
	cd src && \
	javac -d ../bin Main.java

run:
	cd bin && \
	java Main $(ARGS)

jar:
	cd bin && \
	jar cfm ../build/quick.jar ../MANIFEST.MF *
