package com.example.steelinventory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.steelinventory.data.ChannelSpec
import com.example.steelinventory.data.ChannelSpecDao
import kotlinx.coroutines.launch

@Composable
fun ChannelSpecScreen(dao: ChannelSpecDao) {
    val scope = rememberCoroutineScope()
    val specs by dao.getAll().collectAsState(initial = emptyList())
    var size by remember { mutableStateOf("") }
    var declaredWeight by remember { mutableStateOf("") }
    var factoryName by remember { mutableStateOf("") }
    var wingWidth by remember { mutableStateOf("") }
    var webThickness by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("مشخصات فنی ناودانی", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        listOf(
            "سایز" to size,
            "وزن اعلامی" to declaredWeight,
            "نام کارخانه" to factoryName,
            "b (عرض بال)" to wingWidth,
            "s (ضخامت جان)" to webThickness
        ).forEachIndexed { i, (label, value) ->
            OutlinedTextField(
                value = value,
                onValueChange = { v -> when(i) { 0->size=v; 1->declaredWeight=v; 2->factoryName=v; 3->wingWidth=v; 4->webThickness=v } },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }
        Button(
            onClick = {
                val dw = declaredWeight.toDoubleOrNull() ?: return@Button
                val b = wingWidth.toDoubleOrNull() ?: return@Button
                val s = webThickness.toDoubleOrNull() ?: return@Button
                scope.launch { dao.insert(ChannelSpec(size=size, declaredWeight=dw, factoryName=factoryName, wingWidth=b, webThickness=s)) }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("ذخیره") }

        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(specs) { spec ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("سایز: ${spec.size} | وزن: ${spec.declaredWeight}kg | کارخانه: ${spec.factoryName}")
                        Text("b=${spec.wingWidth}mm | s=${spec.webThickness}mm")
                    }
                }
            }
        }
    }
}
