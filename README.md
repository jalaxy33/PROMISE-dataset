# PROMISE dataset

## folder usage
All the data is placed under the `PROMISE/` folder.
- `source-code/`: The source code files of the projects.
- `labeled-data/`: The label csv files of the corresponding source code. `name` and `version` describe the exact project and version of the code. The `name1` indicates the actual file path of the code. The last `bug` column indicates the file-wise label. And the remainder columns are the provided hand-crafted features.
- `embedded-data/`: The extracted AST tokens and token embeddings of each code file. The token nodes are selected. Provided by the PROMISE dataset.
- `token-data/`: The AST tokens extracted by me without selection, which preserve as much information as possible.
- `resources/`: Some useful information. `code-prefix.json` records the actual source code root folders. `token-count.json` records the number of token-extracted files of each project within the `token-data/` folder.

The codes I used to extract AST tokens are placed under `preprossed/` folder. 
- If you want to filter out some token type, go to `preprossed/src/get-ast/ast_utils.py` and uncomment or add the token type you want to filter out. Then run the `preprossed/src/get-ast/main.py`. The extracted tokens will be placed under the `PROMISE/token-data/` by default (and override the old files). 

## source doce
* ant: https://archive.apache.org/dist/ant/source/
* camel: https://github.com/apache/camel/releases
* ivy: No Matching version, https://archive.apache.org/dist/ant/ivy/
* jedit: https://sourceforge.net/projects/jedit/files/jedit/
* log4j: https://archive.apache.org/dist/logging/log4j/, https://github.com/apache/log4j/releases
* lucene: https://github.com/apache/lucene-solr/releases
* poi: https://github.com/apache/poi/releases
* synapse: https://github.com/apache/synapse/releases
* velocity: http://archive.apache.org/dist/velocity/engine/, https://github.com/apache/velocity-engine/releases
* xalan: http://archive.apache.org/dist/xml/xalan-j/, https://github.com/apache/xalan-java/releases
* xerces: https://svn.apache.org/repos/asf/xerces/java/tags/