package com.alvaroquintana.adivinabandera.ui.info.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.animation.NavTransitions
import com.alvaroquintana.adivinabandera.ui.common.LocalInfoGridColumns
import com.alvaroquintana.adivinabandera.ui.common.LocalInfoFiltersExpanded
import com.alvaroquintana.adivinabandera.ui.components.EmptyState
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.LocalWindowSizeClass
import com.alvaroquintana.adivinabandera.ui.theme.getBackgroundGradient
import com.alvaroquintana.adivinabandera.ui.theme.isExpanded
import com.alvaroquintana.adivinabandera.ui.theme.isMedium
import com.alvaroquintana.adivinabandera.utils.Constants.TOTAL_ITEM_EACH_LOAD
import com.alvaroquintana.domain.Country
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import java.util.Calendar

private const val MIN_FILTERED_RESULTS_BEFORE_SCROLL = 24

@Composable
fun InfoListContent(
    countries: List<Country>,
    onCountryClick: (Country) -> Unit,
    currentPage: Int,
    onLoadMore: (Int) -> Unit,
    modifier: Modifier = Modifier,
    forceStack: Boolean = false
) {
    val windowSize = LocalWindowSizeClass.current
    val focusManager = LocalFocusManager.current

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedRegion by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedColumnsState = LocalInfoGridColumns.current
    if (selectedColumnsState.intValue !in 2..4) {
        selectedColumnsState.intValue = if (windowSize.isMedium) 3 else 2
    }

    val filteredList = remember(countries, searchQuery, selectedRegion) {
        val query = searchQuery.trim()
        countries.filter { country ->
            val matchesQuery = query.isBlank() ||
                country.name.contains(query, ignoreCase = true) ||
                country.region.contains(query, ignoreCase = true) ||
                country.capital.contains(query, ignoreCase = true) ||
                country.alpha2Code.contains(query, ignoreCase = true) ||
                country.alpha3Code.contains(query, ignoreCase = true)
            val matchesRegion = selectedRegion == null ||
                country.region.equals(selectedRegion, ignoreCase = true)
            matchesQuery && matchesRegion
        }
    }

    val countryByAlpha3 = remember(countries) {
        countries.associateBy { it.alpha3Code.uppercase() }
    }
    val hasActiveFilters = searchQuery.isNotBlank() || selectedRegion != null

    val featured = remember(countries) {
        if (countries.isEmpty()) null
        else {
            val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            countries[day % countries.size]
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(getBackgroundGradient())
    ) {
        when {
            windowSize.isExpanded && !forceStack -> InfoExpandedLayout(
                filteredList = filteredList,
                countries = countries,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedRegion = selectedRegion,
                onRegionChange = { selectedRegion = it },
                hasActiveFilters = hasActiveFilters,
                totalLoadedCount = countries.size,
                featured = featured,
                onCountryClick = onCountryClick,
                currentPage = currentPage,
                onLoadMore = onLoadMore,
                countryByAlpha3 = countryByAlpha3,
                onClearFocus = { focusManager.clearFocus() }
            )

            else -> InfoStackLayout(
                columns = selectedColumnsState.intValue.coerceIn(2, 4),
                filteredList = filteredList,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedRegion = selectedRegion,
                onRegionChange = { selectedRegion = it },
                hasActiveFilters = hasActiveFilters,
                totalLoadedCount = countries.size,
                featured = featured,
                onCountryClick = onCountryClick,
                currentPage = currentPage,
                onLoadMore = onLoadMore,
                onClearFocus = { focusManager.clearFocus() }
            )
        }
    }
}

@Composable
private fun InfoStackLayout(
    columns: Int,
    filteredList: List<Country>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedRegion: String?,
    onRegionChange: (String?) -> Unit,
    hasActiveFilters: Boolean,
    totalLoadedCount: Int,
    featured: Country?,
    onCountryClick: (Country) -> Unit,
    currentPage: Int,
    onLoadMore: (Int) -> Unit,
    onClearFocus: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val canLoadMore = filteredList.isNotEmpty()
    FilteredPrefetchEffect(
        enabled = hasActiveFilters && filteredList.size < MIN_FILTERED_RESULTS_BEFORE_SCROLL,
        filteredCount = filteredList.size,
        totalLoadedCount = totalLoadedCount,
        currentPage = currentPage,
        onLoadMore = onLoadMore
    )
    PaginationEffect(
        gridState = gridState,
        enabled = canLoadMore,
        currentPage = currentPage,
        onLoadMore = onLoadMore
    )

    InfoGrid(
        state = gridState,
        columns = columns,
        filteredList = filteredList,
        searchQuery = searchQuery,
        onSearchChange = onSearchChange,
        selectedRegion = selectedRegion,
        onRegionChange = onRegionChange,
        featured = if (searchQuery.isBlank() && selectedRegion == null) featured else null,
        onCountryClick = onCountryClick,
        selectedCountryAlpha2 = null,
        onClearFocus = onClearFocus
    )
}

@Composable
private fun InfoExpandedLayout(
    filteredList: List<Country>,
    countries: List<Country>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedRegion: String?,
    onRegionChange: (String?) -> Unit,
    hasActiveFilters: Boolean,
    totalLoadedCount: Int,
    featured: Country?,
    onCountryClick: (Country) -> Unit,
    currentPage: Int,
    onLoadMore: (Int) -> Unit,
    countryByAlpha3: Map<String, Country>,
    onClearFocus: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val canLoadMore = filteredList.isNotEmpty()
    FilteredPrefetchEffect(
        enabled = hasActiveFilters && filteredList.size < MIN_FILTERED_RESULTS_BEFORE_SCROLL,
        filteredCount = filteredList.size,
        totalLoadedCount = totalLoadedCount,
        currentPage = currentPage,
        onLoadMore = onLoadMore
    )
    PaginationEffect(
        gridState = gridState,
        enabled = canLoadMore,
        currentPage = currentPage,
        onLoadMore = onLoadMore
    )

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(0.4f).fillMaxHeight()) {
            InfoGrid(
                state = gridState,
                columns = 2,
                filteredList = filteredList,
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                selectedRegion = selectedRegion,
                onRegionChange = onRegionChange,
                featured = if (searchQuery.isBlank() && selectedRegion == null) featured else null,
                onCountryClick = onCountryClick,
                selectedCountryAlpha2 = null,
                onClearFocus = onClearFocus
            )
        }
        VerticalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.fillMaxHeight()
        )
        Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
            EmptySelectionPlaceholder()
        }
    }
}

@Composable
private fun FilteredPrefetchEffect(
    enabled: Boolean,
    filteredCount: Int,
    totalLoadedCount: Int,
    currentPage: Int,
    onLoadMore: (Int) -> Unit
) {
    LaunchedEffect(enabled, filteredCount, totalLoadedCount, currentPage) {
        if (!enabled) return@LaunchedEffect

        val expectedLoadedCount = (currentPage + 1) * TOTAL_ITEM_EACH_LOAD
        val canRequestNextPage = totalLoadedCount >= expectedLoadedCount
        if (canRequestNextPage) {
            onLoadMore(currentPage + 1)
        }
    }
}

@Composable
private fun PaginationEffect(
    gridState: LazyGridState,
    enabled: Boolean,
    currentPage: Int,
    onLoadMore: (Int) -> Unit
) {
    LaunchedEffect(gridState, currentPage, enabled) {
        if (!enabled) return@LaunchedEffect

        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible to total
        }
            .distinctUntilChanged()
            .filter { (lastVisible, total) -> total > 0 && lastVisible >= total - 2 }
            .collect {
                onLoadMore(currentPage + 1)
            }
    }
}

@Composable
private fun InfoGrid(
    state: LazyGridState,
    columns: Int,
    filteredList: List<Country>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedRegion: String?,
    onRegionChange: (String?) -> Unit,
    featured: Country?,
    onCountryClick: (Country) -> Unit,
    selectedCountryAlpha2: String?,
    onClearFocus: () -> Unit
) {
    val filtersExpanded = LocalInfoFiltersExpanded.current.value

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = state,
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        val hasActiveFilters = searchQuery.isNotBlank() || selectedRegion != null
        if (filtersExpanded || hasActiveFilters) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedVisibility(
                        visible = filtersExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        StickySearchBar(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            onSearch = onClearFocus
                        )
                    }
                    AnimatedVisibility(
                        visible = filtersExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        RegionFilterRow(
                            selectedRegion = selectedRegion,
                            onRegionChange = onRegionChange
                        )
                    }
                    AnimatedVisibility(
                        visible = !filtersExpanded && hasActiveFilters,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        ActiveFiltersRow(
                            searchQuery = searchQuery,
                            selectedRegion = selectedRegion,
                            onClearSearch = {
                                onSearchChange("")
                                onClearFocus()
                            },
                            onClearRegion = { onRegionChange(null) }
                        )
                    }
                }
            }
        }

        if (featured != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                FeaturedCountryBanner(
                    country = featured,
                    onClick = { onCountryClick(featured) }
                )
            }
        }

        if (filteredList.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(message = stringResource(R.string.not_exist))
            }
        } else {
            items(
                items = filteredList,
                key = { country ->
                    country.alpha2Code.ifBlank {
                        country.alpha3Code.ifBlank { country.name }
                    }
                },
                contentType = { "country-card" }
            ) { country ->
                AnimatedVisibility(
                    visible = true,
                    enter = NavTransitions.fadeEnterTransition,
                    exit = NavTransitions.fadeExitTransition
                ) {
                    CountryGridCard(
                        country = country,
                        onClick = { onCountryClick(country) },
                        selected = selectedCountryAlpha2 != null && country.alpha2Code == selectedCountryAlpha2
                    )
                }
            }
        }
    }
}


@Composable
private fun StickySearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        placeholder = {
            Text(
                text = stringResource(R.string.search_country),
                fontFamily = DynaPuffFamily
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RegionFilterRow(
    selectedRegion: String?,
    onRegionChange: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(DefaultRegionOptions) { option ->
            RegionFilterChip(
                option = option,
                selected = selectedRegion == option.key,
                onClick = { onRegionChange(option.key) }
            )
        }
    }
}

@Composable
private fun ActiveFiltersRow(
    searchQuery: String,
    selectedRegion: String?,
    onClearSearch: () -> Unit,
    onClearRegion: () -> Unit
) {
    val removeFilterCd = stringResource(R.string.remove_filter)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (searchQuery.isNotBlank()) {
            InputChip(
                selected = true,
                onClick = onClearSearch,
                label = {
                    Text(
                        text = searchQuery,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 120.dp)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = removeFilterCd,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
        if (selectedRegion != null) {
            val regionOption = DefaultRegionOptions.firstOrNull { it.key == selectedRegion }
            val regionLabel = regionOption?.let { stringResource(it.labelRes) } ?: selectedRegion
            InputChip(
                selected = true,
                onClick = onClearRegion,
                label = {
                    Text(
                        text = regionLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 120.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = removeFilterCd,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}
