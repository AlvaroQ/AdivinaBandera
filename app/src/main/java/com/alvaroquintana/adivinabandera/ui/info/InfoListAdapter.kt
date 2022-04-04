package com.alvaroquintana.adivinabandera.ui.info

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.common.inflate
import com.alvaroquintana.adivinabandera.utils.glideLoadBase64
import com.alvaroquintana.domain.Country
import java.text.NumberFormat
import java.util.*

class InfoListAdapter(
    val context: Context,
    var infoList: MutableList<Country>) : RecyclerView.Adapter<InfoListAdapter.InfoListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoListViewHolder {
        val view = parent.inflate(R.layout.item_info, false)
        return InfoListViewHolder(view)
    }

    override fun onBindViewHolder(holder: InfoListViewHolder, position: Int) {
        val country = infoList[position]

        glideLoadBase64(context,  country.icon, holder.flagImage)

        holder.countryText.text = country.name
        holder.regionText.text = String.format(context.getString(R.string.region), country.region)
        holder.capitalText.text = String.format(context.getString(R.string.capital), country.capital)

        for (currency in country.currencies) {
            holder.currencyText.text = String.format(context.getString(R.string.currencies), currency.name, currency.symbol)
        }

        for (languages in country.languages) {
            holder.languageText.text = String.format(context.getString(R.string.languages), languages.name, languages.nativeName)
        }

        holder.populationText.text = String.format(context.getString(R.string.population), NumberFormat.getNumberInstance(Locale.US).format(country.population).replace(",", "."))
        holder.areaText.text = String.format(context.getString(R.string.area), NumberFormat.getNumberInstance(Locale.US).format(country.area).replace(",", "."))
        holder.phonePrefixText.text = String.format(context.getString(R.string.prefix), country.callingCodes)
    }

    override fun getItemCount(): Int {
        return infoList.size
    }

    fun getItem(position: Int): Country {
        return infoList[position]
    }

    fun update(modelList: MutableList<Country>){
        infoList = modelList
    }

    class InfoListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var flagImage: ImageView = view.findViewById(R.id.flagImage)
        var countryText: TextView = view.findViewById(R.id.countryText)
        var regionText: TextView = view.findViewById(R.id.regionText)
        var capitalText: TextView = view.findViewById(R.id.capitalText)
        var currencyText: TextView = view.findViewById(R.id.currencyIndexText)
        var languageText: TextView = view.findViewById(R.id.languageIndexText)
        var populationText: TextView = view.findViewById(R.id.populationText)
        var areaText: TextView = view.findViewById(R.id.areaText)
        var phonePrefixText: TextView = view.findViewById(R.id.phonePrefixText)
    }
}