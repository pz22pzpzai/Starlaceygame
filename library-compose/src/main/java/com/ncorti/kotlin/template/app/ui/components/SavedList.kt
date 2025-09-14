package com.ncorti.kotlin.template.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Display a list of saved items. Each item can be selected by tapping the row.
 *
 * @param items the list of saved item names to display
 * @param onItemSelected callback triggered when the user selects an item
 * @param modifier optional [Modifier] for this component
 */
@Composable
fun SavedList(
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(items) { item ->
            Text(
                text = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemSelected(item) }
                    .padding(16.dp)
            )
        }
    }
}

