# Introduction #
WebAnno supports several formats for in- and export, which will be explained in detail in the corresponding chapters.



# Plain text #
Plain text is the simple .txt text format and is supported for in- and export in WebAnno. Please note that plain text does not contain any annotation.

# WebAnno TSV format - Old #
WebAnno specific tab separated format with 9 columns. Columns are separated by TAB character and sentences are separated by a blank new line. The individual columns hold the following information:

First column: token Number, in a sentence

Second Column: the token

Third column: the lemma

Fourth column: the POS

Fifth column: Named Entity annotations in BIO(nested and multiple NE annotations separated by "|" character)

Sixth column: the target token for a dependency parsing <br>

Seventh column: the function of the dependency parsing <br>

Eighth and ninth column: No function encoded