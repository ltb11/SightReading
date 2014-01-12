TEX = pdflatex
BIB = bibtex

.PHONY: all clean

all: report.pdf

report.pdf: *.tex
	$(TEX) report.tex


clean: 	
		rm -rf *.pdf
		rm -rf *.log
		rm -rf *.aux
