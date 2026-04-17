package com.alvaroquintana.adivinabandera.cosmetics

import com.alvaroquintana.domain.cosmetics.CosmeticCategory
import com.alvaroquintana.domain.cosmetics.UnlockCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BanderaCatalogTest {

    // ===========================
    // Unicidad de IDs
    // ===========================

    @Test
    fun `todos los items tienen IDs unicos`() {
        val ids = BanderaCatalog.getAllItems().map { it.id }
        val uniqueIds = ids.toSet()

        assertEquals(
            "Se encontraron IDs duplicados en BanderaCatalog",
            ids.size,
            uniqueIds.size
        )
    }

    // ===========================
    // Defaults por categoria
    // ===========================

    @Test
    fun `PROFILE_FRAME tiene al menos un item default`() {
        val defaults = BanderaCatalog.getByCategory(CosmeticCategory.PROFILE_FRAME)
            .filter { it.isDefault }
        assertTrue("PROFILE_FRAME debe tener al menos 1 default", defaults.isNotEmpty())
    }

    @Test
    fun `TITLE_BADGE tiene al menos un item default`() {
        val defaults = BanderaCatalog.getByCategory(CosmeticCategory.TITLE_BADGE)
            .filter { it.isDefault }
        assertTrue("TITLE_BADGE debe tener al menos 1 default", defaults.isNotEmpty())
    }

    @Test
    fun `ANSWER_CARD_THEME tiene al menos un item default`() {
        val defaults = BanderaCatalog.getByCategory(CosmeticCategory.ANSWER_CARD_THEME)
            .filter { it.isDefault }
        assertTrue("ANSWER_CARD_THEME debe tener al menos 1 default", defaults.isNotEmpty())
    }

    @Test
    fun `CELEBRATION_ANIMATION tiene al menos un item default`() {
        val defaults = BanderaCatalog.getByCategory(CosmeticCategory.CELEBRATION_ANIMATION)
            .filter { it.isDefault }
        assertTrue("CELEBRATION_ANIMATION debe tener al menos 1 default", defaults.isNotEmpty())
    }

    // ===========================
    // Defaults son Free
    // ===========================

    @Test
    fun `todos los items marcados como default tienen condicion Free`() {
        val nonFreeDefaults = BanderaCatalog.getAllItems()
            .filter { it.isDefault }
            .filter { it.unlockCondition !is UnlockCondition.Free }

        assertTrue(
            "Estos items son default pero no son Free: ${nonFreeDefaults.map { it.id }}",
            nonFreeDefaults.isEmpty()
        )
    }

    // ===========================
    // getItem — correctitud
    // ===========================

    @Test
    fun `getItem retorna el item correcto para id existente`() {
        val item = BanderaCatalog.getItem("frame_default")

        assertNotNull(item)
        assertEquals("frame_default", item!!.id)
        assertEquals(CosmeticCategory.PROFILE_FRAME, item.category)
        assertTrue(item.isDefault)
    }

    @Test
    fun `getItem retorna null para id inexistente`() {
        val item = BanderaCatalog.getItem("id_que_no_existe_en_el_catalogo")

        assertNull(item)
    }

    @Test
    fun `getItem retorna item correcto de cada categoria`() {
        assertNotNull(BanderaCatalog.getItem("title_default"))
        assertNotNull(BanderaCatalog.getItem("card_default"))
        assertNotNull(BanderaCatalog.getItem("celebration_default"))
        assertNotNull(BanderaCatalog.getItem("icon_bandera_gold"))
    }

    // ===========================
    // getByCategory — correctitud
    // ===========================

    @Test
    fun `getByCategory PROFILE_FRAME retorna exactamente 5 items`() {
        val items = BanderaCatalog.getByCategory(CosmeticCategory.PROFILE_FRAME)
        assertEquals(5, items.size)
    }

    @Test
    fun `getByCategory TITLE_BADGE retorna exactamente 5 items`() {
        val items = BanderaCatalog.getByCategory(CosmeticCategory.TITLE_BADGE)
        assertEquals(5, items.size)
    }

    @Test
    fun `getByCategory ANSWER_CARD_THEME retorna exactamente 3 items`() {
        val items = BanderaCatalog.getByCategory(CosmeticCategory.ANSWER_CARD_THEME)
        assertEquals(3, items.size)
    }

    @Test
    fun `getByCategory CELEBRATION_ANIMATION retorna exactamente 3 items`() {
        val items = BanderaCatalog.getByCategory(CosmeticCategory.CELEBRATION_ANIMATION)
        assertEquals(3, items.size)
    }

    @Test
    fun `getByCategory APP_ICON retorna exactamente 1 item`() {
        val items = BanderaCatalog.getByCategory(CosmeticCategory.APP_ICON)
        assertEquals(1, items.size)
    }

    @Test
    fun `getByCategory retorna solo items de la categoria solicitada`() {
        CosmeticCategory.entries.forEach { category ->
            val items = BanderaCatalog.getByCategory(category)
            assertTrue(
                "Todos los items de getByCategory($category) deben pertenecer a esa categoria",
                items.all { it.category == category }
            )
        }
    }

    // ===========================
    // Las 5 categorias estan representadas
    // ===========================

    @Test
    fun `todas las 5 categorias cosmeticas estan representadas en el catalogo`() {
        val categoriesInCatalog = BanderaCatalog.getAllItems()
            .map { it.category }
            .toSet()

        assertTrue("Falta PROFILE_FRAME", CosmeticCategory.PROFILE_FRAME in categoriesInCatalog)
        assertTrue("Falta TITLE_BADGE", CosmeticCategory.TITLE_BADGE in categoriesInCatalog)
        assertTrue("Falta ANSWER_CARD_THEME", CosmeticCategory.ANSWER_CARD_THEME in categoriesInCatalog)
        assertTrue("Falta CELEBRATION_ANIMATION", CosmeticCategory.CELEBRATION_ANIMATION in categoriesInCatalog)
        assertTrue("Falta APP_ICON", CosmeticCategory.APP_ICON in categoriesInCatalog)
    }

    @Test
    fun `el catalogo tiene exactamente 17 items`() {
        assertEquals(17, BanderaCatalog.getAllItems().size)
    }

    // ===========================
    // Precios positivos para items comprables
    // ===========================

    @Test
    fun `todos los items PurchaseWithCoins tienen precio positivo`() {
        val coinItems = BanderaCatalog.getAllItems()
            .filter { it.unlockCondition is UnlockCondition.PurchaseWithCoins }

        assertTrue("Debe haber items con coins en el catalogo", coinItems.isNotEmpty())
        coinItems.forEach { item ->
            val price = (item.unlockCondition as UnlockCondition.PurchaseWithCoins).price
            assertTrue(
                "El item '${item.id}' tiene precio de coins negativo o cero: $price",
                price > 0
            )
        }
    }

    @Test
    fun `todos los items PurchaseWithGems tienen precio positivo`() {
        val gemItems = BanderaCatalog.getAllItems()
            .filter { it.unlockCondition is UnlockCondition.PurchaseWithGems }

        assertTrue("Debe haber items con gems en el catalogo", gemItems.isNotEmpty())
        gemItems.forEach { item ->
            val price = (item.unlockCondition as UnlockCondition.PurchaseWithGems).price
            assertTrue(
                "El item '${item.id}' tiene precio de gems negativo o cero: $price",
                price > 0
            )
        }
    }

    // ===========================
    // Items especificos del catalogo
    // ===========================

    @Test
    fun `frame_default es default y Free en PROFILE_FRAME`() {
        val item = BanderaCatalog.getItem("frame_default")!!
        assertTrue(item.isDefault)
        assertTrue(item.unlockCondition is UnlockCondition.Free)
        assertEquals(CosmeticCategory.PROFILE_FRAME, item.category)
    }

    @Test
    fun `title_legend requiere ReachLevel 50`() {
        val item = BanderaCatalog.getItem("title_legend")!!
        val condition = item.unlockCondition
        assertTrue(condition is UnlockCondition.ReachLevel)
        assertEquals(50, (condition as UnlockCondition.ReachLevel).level)
    }

    @Test
    fun `icon_bandera_gold pertenece a APP_ICON y requiere gems`() {
        val item = BanderaCatalog.getItem("icon_bandera_gold")!!
        assertEquals(CosmeticCategory.APP_ICON, item.category)
        assertTrue(item.unlockCondition is UnlockCondition.PurchaseWithGems)
    }

    @Test
    fun `title_geografo requiere StreakMilestone de 14 dias`() {
        val item = BanderaCatalog.getItem("title_geografo")!!
        val condition = item.unlockCondition
        assertTrue(condition is UnlockCondition.StreakMilestone)
        assertEquals(14, (condition as UnlockCondition.StreakMilestone).days)
    }
}
