package com.example.hw2

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject



data class Card(
    var name: String,
    var layout : String,
    var type: String,
    var supertype: String,
    var rarity: String,
    var colors: List<Color>,
    var sep:String = "---------------------------------------------------------------------------------- \n"

)

data class Color(
    var color: String
)

fun toString(card: Card): String {
    return  "Name: ${card.name} \n" +
            "Layout: ${card.layout} \n" +
            "Type: ${card.type} \n" +
            "Supertype: ${card.supertype} \n" +
            "Rarity: ${card.rarity} \n" +
            "Color: ${card.colors[0].color} \n ${card.sep}"
}

suspend fun sortedListToString(cardList: MutableList<Card>): String = withContext(Dispatchers.Default) {
    var result = ""
    for (card in cardList.sortedBy { it.name }) {
        result += toString(card)
    }
    return@withContext result
}

suspend fun parseJsonAddToCardList(jsonText:String): MutableList<Card> = withContext(Dispatchers.Default) {
    val cardList: MutableList<Card> = mutableListOf()
    val mainObject = JSONObject(jsonText)
    val cardsArray = mainObject.getJSONArray("cards")

    for(i in 0  until cardsArray.length()){
        val card = cardsArray.getJSONObject(i)
        for(k in 0 until (card.optJSONArray("colors")?.length() ?: 0)) {
            cardList.add(
                Card(
                    card.optString("name"),
                    card.optString("layout"),
                    card.optString("type"),
                    card.optString("supertype"),
                    card.optString("rarity"),

                    listOf(
                        Color(
                            card.optJSONArray("colors")?.getString(k) ?: ""
                        )
                    )
                )
            )
        }
    }
    return@withContext cardList
}
