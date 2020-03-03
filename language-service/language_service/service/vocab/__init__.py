from wiktionaryparser import WiktionaryParser
from dto import Definition

parser = WiktionaryParser()


def get_definition(language, word):
    response = parser.fetch(word, language)

    definitions = []
    for entry in response:
        if "definitions" in entry:
            for definition in entry["definitions"]:
                definitions.append(parse_definition(definition))

    return definitions


def parse_definition(definition):
    subdefinitions = definition["text"] if "text" in definition else None
    tag = definition["partOfSpeech"] if "partOfSpeech" in definition else None
    examples = definition["examples"] if "examples" in definition else None
    return Definition(subdefinitions=subdefinitions, tag=tag, examples=examples)