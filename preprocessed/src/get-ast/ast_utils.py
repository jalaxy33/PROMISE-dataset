"""ast_utils.py
- demo & test: '../../demo/get-ast/get_ast.py'
"""

import os
import javalang


def filter_condition(token):
    not_condition = (
        # isinstance(token, javalang.tokenizer.Separator)
        # or isinstance(token, javalang.tokenizer.Operator)
        # or isinstance(token, javalang.tokenizer.String)
        # or issubclass(type(token), javalang.tokenizer.Literal)
    )
    return not not_condition


def get_file_tokens(srcfile: str):
    with open(srcfile, encoding="utf-8") as f:
        contents = f.read()
    tokens = javalang.tokenizer.tokenize(contents)  # return generator
    tokens = list(filter(filter_condition, tokens))
    return tokens


def save_file_tokens(trgfile: str, tokens: list):
    trgdir = "/".join(trgfile.split("/")[:-1])
    if not os.path.exists(trgdir):
        os.makedirs(trgdir)

    token_values = list(tok.value + "\n" for tok in tokens)
    with open(trgfile, "w") as f:
        f.writelines(token_values)


def process_file_tokens(srcfile: str, trgfile: str):
    tokens = get_file_tokens(srcfile)
    save_file_tokens(trgfile, tokens)
