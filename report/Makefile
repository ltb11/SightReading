TEX = pdflatex
BIB = bibtex

.PHONY: all clean

all: report.pdf

report.pdf: *.tex ./assets/*
	$(TEX) report.tex
report.toc:
	$(TEX) report.tex


clean: 	
		rm -rf *.pdf
		rm -rf *.log
		rm -rf *.toc
		rm -rf *.aux
		rm -rf *.log
		rm -rf *.lof
		rm -rf *.out
