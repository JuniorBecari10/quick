all: compile run

compile:
	cd src && \
	javac -d ../bin Main.java

run:
	cd bin && \
	java Main

jar:
	cd bin && \
	jar cfm ../build/quick.jar ../MANIFEST.MF *
