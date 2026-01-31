package com.dicemonger.perchancescifi.model

class TagsCreator {
    val list = mutableListOf<Tag>()

    fun CreateTags(): List<Tag> {
        list.clear()
        addMajorCategories()
        addRegions()
        addCountries()
        addFiction()
        return list
    }

    private fun addMajorCategories() {
        list.add(Tag("Mythology", "mythic", null))
        list.add(Tag("Mythic Person", "mperson", "mythic", priority = 0))
        list.add(Tag("Mythic Place", "mplace", "mythic", priority = 0))
        list.add(Tag("Places", "place", null))
        list.add(Tag("Names", "name", null))
        list.add(Tag("People", "person", null))
        list.add(Tag("Words", "word", null))
        list.add(Tag("Fiction", "fiction", null))
    }

    private fun addRegions() {
        list.add(Tag("Europe", "europe", "place", 1))
    }

    private fun addCountries() {
        list.add(Tag("England", "england", "place"))
        list.add(Tag("Wales", "wales", "place"))
    }

    private fun addFiction() {
        list.add(Tag("Arthurian", "arthurian", "fiction"))
        list.add(Tag("Dune", "dune", "fiction"))
    }
}