package com.terryreed.swipelist

import org.junit.Assert.assertEquals
import org.junit.Test

class GroceryCategorizerTest {

    @Test
    fun `groups items by category`() {
        val items = listOf(
            "Cucumber", "Onions", "Pineapple", "Mushrooms", // produce
            "Bread", // bakery
            "Chicken", // meat
            "Salmon", // fish
            "Milk", "Cheese", // dairy
            "Eggs", // eggs
            "Ice Cream", // frozen
            "Soda", // drinks
            "Beer", // alcohol
            "Bleach", // household
            "Nappies", // baby
            "Dog Food" // pet
        )
        val groups = GroceryCategorizer.groupByCategory(items)
        assertEquals(
            listOf("Cucumber", "Onions", "Pineapple", "Mushrooms"),
            groups[GroceryCategorizer.Category.PRODUCE]
        )
        assertEquals(listOf("Bread"), groups[GroceryCategorizer.Category.BAKERY])
        assertEquals(listOf("Chicken"), groups[GroceryCategorizer.Category.MEAT])
        assertEquals(listOf("Salmon"), groups[GroceryCategorizer.Category.FISH])
        assertEquals(listOf("Milk", "Cheese"), groups[GroceryCategorizer.Category.DAIRY])
        assertEquals(listOf("Eggs"), groups[GroceryCategorizer.Category.EGGS])
        assertEquals(listOf("Ice Cream"), groups[GroceryCategorizer.Category.FROZEN])
        assertEquals(listOf("Soda"), groups[GroceryCategorizer.Category.DRINKS])
        assertEquals(listOf("Beer"), groups[GroceryCategorizer.Category.ALCOHOL])
        assertEquals(listOf("Bleach"), groups[GroceryCategorizer.Category.HOUSEHOLD])
        assertEquals(listOf("Nappies"), groups[GroceryCategorizer.Category.BABY])
        assertEquals(listOf("Dog Food"), groups[GroceryCategorizer.Category.PET])
    }

    @Test
    fun `allows adding keywords to categories`() {
        GroceryCategorizer.addKeywords(
            GroceryCategorizer.Category.DRINKS,
            listOf("kombucha")
        )
        val items = listOf("Kombucha", "Bread")
        val groups = GroceryCategorizer.groupByCategory(items)
        assertEquals(listOf("Kombucha"), groups[GroceryCategorizer.Category.DRINKS])
        assertEquals(listOf("Bread"), groups[GroceryCategorizer.Category.BAKERY])
    }

    @Test
    fun `returns keywords for a category`() {
        GroceryCategorizer.addKeywords(
            GroceryCategorizer.Category.DRINKS,
            listOf("kombucha")
        )
        val keywords = GroceryCategorizer.keywordsFor(GroceryCategorizer.Category.DRINKS)
        assertEquals(true, keywords.contains("kombucha"))
    }

    @Test
    fun `categorizes additional frozen items`() {
        val items = listOf(
            "Frozen raspberries",
            "Ben & Jerry's"
        )
        val groups = GroceryCategorizer.groupByCategory(items)
        assertEquals(
            listOf("Frozen raspberries", "Ben & Jerry's"),
            groups[GroceryCategorizer.Category.FROZEN]
        )
    }

    @Test
    fun `categorizes additional baby items`() {
        val items = listOf(
            "Baby shampoo",
            "Nipple cream",
            "Baby bath thermometer",
            "Baby wipes"
        )
        val groups = GroceryCategorizer.groupByCategory(items)
        assertEquals(
            listOf(
                "Baby shampoo",
                "Nipple cream",
                "Baby bath thermometer",
                "Baby wipes"
            ),
            groups[GroceryCategorizer.Category.BABY]
        )
    }

    @Test
    fun `categorizes additional drinks items`() {
        val items = listOf(
            "Coca-Cola",
            "Ginger beer",
            "Evian"
        )
        val groups = GroceryCategorizer.groupByCategory(items)
        assertEquals(
            listOf(
                "Coca-Cola",
                "Ginger beer",
                "Evian"
            ),
            groups[GroceryCategorizer.Category.DRINKS]
        )
    }

    @Test
    fun `categorizes additional meat items`() {
        val items = listOf(
            "Chicken breasts",
            "Turkey mince",
            "Beef steaks",
            "Lamb chops",
            "Pork sausages",
            "Gammon joint",
            "Bacon rashers",
            "Ham slices",
            "Duck breast",
            "Goose breast",
            "Venison steaks",
            "Rabbit meat",
            "Goat meat",
            "Bacon"
        )
        val groups = GroceryCategorizer.groupByCategory(items)
        assertEquals(
            items,
            groups[GroceryCategorizer.Category.MEAT]
        )
    }

    @Test
    fun `categorizes additional fish items`() {
        val items = listOf(
            "Cod fillets",
            "Prawns raw",
            "Crab meat",
            "Smoked salmon",
            "Tuna steaks"
        )
        val groups = GroceryCategorizer.groupByCategory(items)
        assertEquals(
            items,
            groups[GroceryCategorizer.Category.FISH]
        )
    }

    @Test
    fun `categorizes additional dairy items`() {
        val items = listOf(
            "Whole milk",
            "Greek yogurt",
            "Salted butter",
            "Mature cheddar",
            "Yazoo",
            "Custard"
        )
        val groups = GroceryCategorizer.groupByCategory(items)
        assertEquals(
            items,
            groups[GroceryCategorizer.Category.DAIRY]
        )
    }

    @Test
    fun `categorizes additional egg items`() {
        val items = listOf(
            "Free range eggs",
            "Duck eggs"
        )
        val groups = GroceryCategorizer.groupByCategory(items)
        assertEquals(
            items,
            groups[GroceryCategorizer.Category.EGGS]
        )
    }

    @Test
    fun `categorizes additional household items`() {
        val items = listOf(
            "Toilet roll",
            "Kitchen cleaner",
            "Batteries"
        )
        val groups = GroceryCategorizer.groupByCategory(items)
        assertEquals(
            items,
            groups[GroceryCategorizer.Category.HOUSEHOLD]
        )
    }

    @Test
    fun `categorizes additional alcohol items`() {
        val items = listOf(
            "Lager",
            "Prosecco",
            "Smirnoff",
            "Bourbon"
        )
        val groups = GroceryCategorizer.groupByCategory(items)
        assertEquals(
            items,
            groups[GroceryCategorizer.Category.ALCOHOL]
        )
    }

    @Test
    fun `prioritizes specific matches over generic alcohol keywords`() {
        val items = listOf(
            "Kale",
            "Bourbon biscuit"
        )
        val groups = GroceryCategorizer.groupByCategory(items)
        assertEquals(
            listOf("Kale"),
            groups[GroceryCategorizer.Category.PRODUCE]
        )
        assertEquals(
            listOf("Bourbon biscuit"),
            groups[GroceryCategorizer.Category.BAKERY]
        )
    }

    @Test
    fun `categorizes additional pet items`() {
        val items = listOf(
            "Dry dog food",
            "Cat litter tray",
            "Rabbit food",
            "Fish food",
            "Hay"
        )
        val groups = GroceryCategorizer.groupByCategory(items)
        assertEquals(
            items,
            groups[GroceryCategorizer.Category.PET]
        )
    }

    @Test
    fun `allows removing keywords from categories`() {
        GroceryCategorizer.addKeywords(
            GroceryCategorizer.Category.BAKERY,
            listOf("tempbakery")
        )
        GroceryCategorizer.removeKeyword(
            GroceryCategorizer.Category.BAKERY,
            "tempbakery"
        )
        val keywords = GroceryCategorizer.keywordsFor(GroceryCategorizer.Category.BAKERY)
        assertEquals(false, keywords.contains("tempbakery"))
        assertEquals(
            GroceryCategorizer.Category.OTHER,
            GroceryCategorizer.categoryFor("tempbakery")
        )
    }
}
