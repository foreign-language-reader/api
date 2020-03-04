"""
Where you put definition business logic.
We combine different data sources as available to deliver the best definitions.
"""
from language_service.service.definition.cedict import (
    get_definitions as get_cedict_definitions,
)
from language_service.service.definition.wiktionary import (
    get_definitions as get_wiktionary_definitions,
)


def get_definitions(language, word):
    if language == "CHINESE":
        wiktionary_definitions = get_wiktionary_definitions(language, word)

        cedict_definition = get_cedict_definitions(word)
        if cedict_definition:
            print("Attaching cedict definition")
            definitions = []
            for definition in wiktionary_definitions:
                # The CEDICT definitions are more focused than wiktionary so we should prefer them.
                improved_definition = ChineseDefinition(
                    subdefinitions=cedict_definition["definitions"],
                    tag=definition.tag,
                    examples=definition.tag,
                    pinyin=cedict_definition["pinyin"],
                    simplified=cedict_definition["simplified"],
                    traditional=cedict_definition["traditional"],
                )
                definitions.append(improved_definition)
            return definitions
        else:
            return wiktionary_definitions

    elif language == "ENGLISH":
        return get_wiktionary_definitions(language, word)

    elif language == "SPANISH":
        return get_wiktionary_definitions(language, word)

    else:
        raise NotImplementedError("Unknown language requested: %s")
