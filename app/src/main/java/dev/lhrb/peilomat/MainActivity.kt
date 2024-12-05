package dev.lhrb.peilomat

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import dev.lhrb.peilomat.ui.theme.PeilomatTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationProvider = LocationProviderImpl(fusedLocationClient)
        val peilomatBL = PeilomatBL(locationProvider)
        val factor = PeilomatViewModelFactory(peilomatBL)
        val peilomatViewModel = ViewModelProvider(this, factor).get(PeilomatViewModel::class.java)

        enableEdgeToEdge()
        setContent {
            PeilomatTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            colors = topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text("Peilomat 3000")
                            }
                        )

                    },
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CheckLocationPermission()
                    Greeting(
                        viewModel = peilomatViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CheckLocationPermission() {
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val currentPermissionState = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (PackageManager.PERMISSION_GRANTED != currentPermissionState) {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

@Composable
fun Greeting(
    viewModel: PeilomatViewModel,
    modifier: Modifier = Modifier
    ) {
    val peilomatUiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.padding(8.dp)) {
        val (lat, lon, rw, hw) = peilomatUiState.currentPositionData
        CurrentPosition(lat, lon, rw, hw, viewModel::refresh)

        HorizontalDivider(
            thickness = 2.dp,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 12.dp)
        )

        TransformRWHWtoLatLon(
            lat = peilomatUiState.convertRwHwToLatLonData.lat,
            lon = peilomatUiState.convertRwHwToLatLonData.lon,
            onClickConvert = viewModel::transformHwRw
        )

        HorizontalDivider(
            thickness = 2.dp,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 12.dp)
        )

        TransformAngleAndDistanceToLatLon(
            lat = peilomatUiState.convertAngleAndDistanceToLatLonData.lat,
            lon = peilomatUiState.convertAngleAndDistanceToLatLonData.lon,
            onClickConvert = viewModel::transformAngleDistance
        )
    }
}



@Composable
fun TransformAngleAndDistanceToLatLon(
    lat: Double,
    lon: Double,
    onClickConvert: (useAngle: Boolean,
                     angle: String,
                     distance: String,
                     useGivenPoints: Boolean,
                     easting: String,
                     northing: String
            ) -> Unit
) {
    var useAngle by remember { mutableStateOf(true) }
    var distance by remember { mutableStateOf("") }
    var angle by remember { mutableStateOf("") }
    var easting by remember { mutableStateOf("") }
    var northing by remember { mutableStateOf("") }
    var useGivenPoints by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = easting,
                onValueChange = { easting = it },
                label = { Text("RW") },
                readOnly = !useGivenPoints,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)

            )

            OutlinedTextField(
                value = northing,
                onValueChange = { northing = it },
                label = { Text("HW") },
                readOnly = !useGivenPoints,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, end = 4.dp)
            )

            Switch(
                checked = useGivenPoints,
                onCheckedChange = { useGivenPoints = it },
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
                thumbContent = if (useGivenPoints) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Gradzahl verwenden: ",
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
            )
            Switch(
                checked = useAngle,
                onCheckedChange = { useAngle = it },
                thumbContent = if (useAngle) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                }
            )
        }

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {

            OutlinedTextField(
                value = angle,
                onValueChange = { angle = it },
                label = { Text(text = if (useAngle) "Grad" else "MZ") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)

            )

            OutlinedTextField(
                value = distance,
                onValueChange = { distance = it },
                label = { Text("Distanz in m") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
        }

        OutlinedTextField(
            value = "$lat,$lon",
            onValueChange = {},
            label = { Text("lat,lon") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onClickConvert(useAngle, angle, distance, useGivenPoints, easting, northing) },
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
        ) {
            Text("Konvertieren")
        }

    }
}

@Composable
fun TransformRWHWtoLatLon(
    lat: Double,
    lon: Double,
    onClickConvert: (rw: String, hw: String) -> Unit
) {
    var easting by remember { mutableStateOf("") }
    var northing by remember { mutableStateOf("") }

    Column {
        Text("Convert RW,HW to Lat,Lon")

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {

            OutlinedTextField(
                value = easting,
                onValueChange = { easting = it },
                label = { Text("RW") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)

            )

            OutlinedTextField(
                value = northing,
                onValueChange = { northing = it },
                label = { Text("HW") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
        }

        OutlinedTextField(
            value = "$lat,$lon",
            onValueChange = {},
            label = { Text("lat,lon") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onClickConvert(easting, northing) },
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
        ) {
            Text("Konvertieren")
        }
    }
}


@Composable
fun CurrentPosition(
    lat: Double,
    lon: Double,
    easting: Int,
    northing: Int,
    onClickRefresh: () -> Unit
) {
    Column {
        Text("Aktuelle Position:")
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            OutlinedTextField(
                value = "$lat,$lon",
                onValueChange = {},
                label = { Text("lat,lon") },
                readOnly = true,
                modifier = Modifier
                    .weight(0.875f)
                    .padding(end = 4.dp)
            )
            FilledIconButton(
                onClick = { onClickRefresh() },
                modifier = Modifier
                    .weight(0.125f)
                    .align(Alignment.CenterVertically)
                    .padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }


        Row(horizontalArrangement = Arrangement.SpaceEvenly) {

            OutlinedTextField(
                value = "$easting",
                onValueChange = {},
                label = { Text("RW") },
                readOnly = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)

            )

            OutlinedTextField(
                value = "$northing",
                onValueChange = {},
                label = { Text("HW") },
                readOnly = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val peilomatBL = PeilomatBL(object : LocationProvider {
        override suspend fun getLocation(): Coordinates {
            return Coordinates(1.0, 1.0)
        }
    })
    val viewModel = PeilomatViewModel(peilomatBL)

    PeilomatTheme {
        Greeting(viewModel)
    }
}