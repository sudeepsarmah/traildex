package com.example.traildex.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.traildex.data.TrailPoint
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TrailMap(
    modifier: Modifier = Modifier,
    center: TrailPoint?,
    track: List<TrailPoint>,
    plannedStart: TrailPoint?,
    plannedEnd: TrailPoint?,
    focusRouteId: String? = null,
    followLocation: Boolean = false,
    onMapTap: (Double, Double) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.userAgentString = "TrailDexAndroid/1.0 (open-source trail demo)"
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun pointSelected(lat: Double, lon: Double) = onMapTap(lat, lon)
                }, "TrailDex")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) { view.post { view.evaluateJavascript("window.renderTrailDex && renderTrailDex()", null) } }
                }
                loadDataWithBaseURL("https://traildex.local/", MAP_HTML, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.evaluateJavascript("window.setTrailDexData && setTrailDexData(${dataJson(center, track, plannedStart, plannedEnd, focusRouteId, followLocation)})", null)
        }
    )
}

private fun dataJson(center: TrailPoint?, track: List<TrailPoint>, start: TrailPoint?, end: TrailPoint?, focusRouteId: String?, followLocation: Boolean): String {
    val value = JSONObject()
        .put("center", center?.let { JSONObject().put("lat", it.latitude).put("lon", it.longitude) })
        .put("track", JSONArray().apply { track.forEach { put(JSONArray().put(it.latitude).put(it.longitude)) } })
        .put("start", start?.let { JSONArray().put(it.latitude).put(it.longitude) })
        .put("end", end?.let { JSONArray().put(it.latitude).put(it.longitude) })
        .put("focus", focusRouteId)
        .put("follow", followLocation)
    return value.toString()
}

private val MAP_HTML = """<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=3,user-scalable=yes"><link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"><style>html,body,#map{height:100%;width:100%;margin:0;background:#eef2e9} .leaflet-control-attribution{font-size:10px!important} .leaflet-container{font-family:monospace}</style><script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script></head><body><div id="map"></div><script>
let map,trackLine,planLine,startMarker,endMarker,userMarker,lastData=null,lastFocus=null;
function renderTrailDex(){if(!window.L)return;map=L.map('map',{zoomControl:true}).setView([0,0],15);L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'© OpenStreetMap contributors'}).addTo(map);map.on('click',e=>{if(window.TrailDex)TrailDex.pointSelected(e.latlng.lat,e.latlng.lng)});if(lastData)setTrailDexData(lastData);setTimeout(()=>map.invalidateSize(),150)}
function setTrailDexData(d){lastData=d;if(!map)return;const c=d.center;if(c&&c.lat&&c.lon){if(userMarker)userMarker.setLatLng([c.lat,c.lon]);else{map.setView([c.lat,c.lon],15);userMarker=L.circleMarker([c.lat,c.lon],{radius:7,color:'#08796b',fillColor:'#7de5d4',fillOpacity:1}).addTo(map).bindPopup('Your current location')}if(d.follow)map.panTo([c.lat,c.lon],{animate:false})}
if(trackLine){map.removeLayer(trackLine);trackLine=null}if(d.track&&d.track.length>1){trackLine=L.polyline(d.track,{color:'#87a900',weight:5}).addTo(map);if(d.focus&&d.focus!==lastFocus)map.fitBounds(trackLine.getBounds(),{padding:[20,20]})}lastFocus=d.focus;
if(startMarker)map.removeLayer(startMarker);if(endMarker)map.removeLayer(endMarker);if(planLine)map.removeLayer(planLine);if(d.start)startMarker=L.marker(d.start).addTo(map).bindPopup('Planned start');if(d.end)endMarker=L.marker(d.end).addTo(map).bindPopup('Planned finish');if(d.start&&d.end)planLine=L.polyline([d.start,d.end],{color:'#a93e5c',dashArray:'7 7',weight:4}).addTo(map);}
</script></body></html>"""
