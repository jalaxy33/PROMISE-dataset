# All-in-one PROMISE software defect dataset

We merge the source code and the labels of each version into one csv file, so you can load the data and labels in once. 
We divide the data by two rules: versions and train-test splits (placed in `projects/` and `splits/` respectively).

## data usage

The data files are essentially compressed csv files. You can load the data simply by something like `pd.read_csv(xxx.csv.gz)`.


## Lists of data

**[ train-test splits ]**

| split | file num |
| -- | -- |
| train-split | 7813 |
| test-split | 4916 |

> List of test split:
> 
> ant-1.7, camel-1.6, ivy-2.0, jedit-4.3, log4j-1.2, lucene-2.4, pbeans-2.0, poi-3.0, synapse-1.2, velocity-1.6, xalan-2.6, xerces-1.3


**[ versions ]**
 
| version | file num |
| -- | -- | 
| ant-1.3      |  116  |
| ant-1.4      |  166  |
| ant-1.5      |  270  |
| ant-1.6      |  350  |
| ant-1.7      |  741  |
| camel-1.0    |  195  |
| camel-1.2    |  368  |
| camel-1.4    |  473  |
| camel-1.6    |  523  |
| ivy-1.1      |  111  |
| ivy-1.4      |  241  |
| ivy-2.0      |  352  |
| jedit-3.2    |  248  |
| jedit-4.0    |  281  |
| jedit-4.1    |  266  |
| jedit-4.2    |  355  |
| jedit-4.3    |  487  |
| log4j-1.0    |  115  |
| log4j-1.1    |  100  |
| log4j-1.2    |  188  |
| lucene-2.4   |  330  |
| lucene-2.2   |  234  |
| lucene-2.0   |  186  |
| pbeans-1.0   |  26   | 
| pbeans-2.0   |  51   | 
| poi-1.5      |  235  |
| poi-2.0      |  309  |
| poi-2.5      |  380  |
| poi-3.0      |  438  |
| synapse-1.0  |  157  |
| synapse-1.1  |  205  |
| synapse-1.2  |  256  |
| velocity-1.4 |  192  |
| velocity-1.5 |  214  |
| velocity-1.6 |  229  | 
| xalan-2.4    |  668  |
| xalan-2.5    |  754  |
| xalan-2.6    |  875  |
| xerces-init  |  162  |
| xerces-1.2   |  436  |
| xerces-1.3   |  446  |




