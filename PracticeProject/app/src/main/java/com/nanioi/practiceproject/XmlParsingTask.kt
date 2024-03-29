package com.nanioi.practiceproject

import android.graphics.Color
import android.os.AsyncTask
import android.util.Log
import android.util.Xml
import android.widget.TextView
import androidx.core.graphics.blue
import androidx.core.graphics.red
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

// 이게 xml파싱해오는 부분인데
private val ns: String? = null

data class ResponseElement( // 받아올 데이터들만 클래스 만들고
        val clearCnt: String?,
        val deathCnt: String?,
        val decideCnt: String?,
        val examCnt: String?,
        val stateDt: String?,
        val stateTime: String?
)

fun parsingData(setdt : Int): XmlPullParser {

    // 그 캘린더 누른 날짜 기준으로 보여줄건데 초기에는 오늘 날짜를 받아와야해서
    //오늘날짜 받아오는 부
    var dt : Int = 0
    val cal = Calendar.getInstance()
    dt += cal.get(Calendar.YEAR)
    dt *= 100
    dt += cal.get((Calendar.MONTH)+1)
    dt *= 100
    dt += cal.get(Calendar.DATE)

    //이게 오픈 url 설정부분인
    val apiKey = "Tdo19TVJxANWay1HQ1dxcwGA5sJ73wYF%2FVfvQaLyA1iBPWkttg74N9jzEUDGlG6J3ItutuWKuzOutjEblPuQIg%3D%3D"
    var pageNo = "1"
    val numOfRows = "10"
    val startCreateDt: String = "20200310" // 이날부터
    var endCreateDt: String = dt.toString() // 그 캘린더로 선택된 날짜 까지

    if(setdt !=0)  endCreateDt  = setdt.toString()
    var keyDecode = URLDecoder.decode(apiKey, "UTF-8")
    val urlBuilder = StringBuilder("http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19InfStateJson") /*URL*/
    urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + URLEncoder.encode(keyDecode, "UTF-8")) /*공공데이터포털에서 받은 인증키*/
    urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(pageNo, "UTF-8")) /*페이지번호*/
    urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(numOfRows, "UTF-8")) /*한 페이지 결과 수*/
    urlBuilder.append("&" + URLEncoder.encode("startCreateDt", "UTF-8") + "=" + URLEncoder.encode(startCreateDt, "UTF-8")) /*검색할 생성일 범위의 시작*/
    urlBuilder.append("&" + URLEncoder.encode("endCreateDt", "UTF-8") + "=" + URLEncoder.encode(endCreateDt, "UTF-8")) /*검색할 생성일 범위의 종료*/

    val url: URL = URL(urlBuilder.toString())
    // url 설정하고


    // 구글링해서 퍼오넉라 xmlPArser 생성해주는 부분같음
    val parser: XmlPullParser = Xml.newPullParser()
    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
    parser.setInput(InputStreamReader(url.openStream(), "UTF-8"))
    parser.nextTag()

    return parser
}

@Throws(XmlPullParserException::class, IOException::class)
fun readFeed(parser: XmlPullParser): List<ResponseElement> { // 이거 Accumulate랑 New에서 doInBackgourd에서 이 함수 호출하는거
    val entries = mutableListOf<ResponseElement>() // 그 받아오는 데이터 클래스로 리스트 만들고

    parser.require(XmlPullParser.START_TAG, ns, "response") // 그 xml 데이터 부분 보면 알텐디 <response> 그부분으로 들어가서 아래 코드 태그들로 타고 들어가서 원하는 데이터들 뽑아오는 부분이
    while (parser.next() != XmlPullParser.END_TAG) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            continue
        }
        if (parser.name == "item") {
            entries.add(readEntry(parser))
        } else {
            skip(parser)
        }
    }
    return entries
}

@Throws(XmlPullParserException::class, IOException::class)
fun readEntry(parser: XmlPullParser): ResponseElement {
    parser.require(XmlPullParser.START_TAG, ns, "item")
    var clearCnt: String? = null
    var deathCnt: String? = null
    var decideCnt: String? = null
    var examCnt: String? = null
    var stateDt: String? = null
    var stateTime: String? = null

    while (parser.next() != XmlPullParser.END_TAG) { // 태그들 보며 이동하면서 원하는 태그들에 데이터 뽑아오는 부
        if (parser.eventType != XmlPullParser.START_TAG) {
            continue
        }
        when (parser.name) {
            "clearCnt" -> clearCnt = readClearCnt(parser)
            "deathCnt" -> deathCnt = readDeathCnt(parser)
            "decideCnt" -> decideCnt = readDecideCnt(parser)
            "examCnt" -> examCnt = readExamCnt(parser)
            "stateDt" -> stateDt = readStateDt(parser)
            "stateTime" -> stateTime = readStateTime(parser)
            else -> skip(parser)
        }
    }
    return ResponseElement(clearCnt, deathCnt, decideCnt, examCnt, stateDt, stateTime) // 클래스에 데이터 넣어서 반환
}

// Processes title tags in the feed.
// 노가다로 해당 태그 데이터 뽑아오는겨
@Throws(IOException::class, XmlPullParserException::class)
fun readStateDt(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, ns, "stateDt")
    val stateDt = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, "stateDt")
    return stateDt
}

// Processes link tags in the feed.
@Throws(IOException::class, XmlPullParserException::class)
fun readClearCnt(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, ns, "clearCnt")
    val clearCnt = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, "clearCnt")
    return clearCnt
}

// Processes summary tags in the feed.
@Throws(IOException::class, XmlPullParserException::class)
fun readStateTime(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, ns, "stateTime")
    val stateTime = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, "stateTime")
    return stateTime
}

// Processes title tags in the feed.
@Throws(IOException::class, XmlPullParserException::class)
fun readDeathCnt(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, ns, "deathCnt")
    val deathCnt = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, "deathCnt")
    return deathCnt
}

// Processes link tags in the feed.
@Throws(IOException::class, XmlPullParserException::class)
fun readDecideCnt(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, ns, "decideCnt")
    val decideCnt = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, "decideCnt")
    return decideCnt
}

// Processes summary tags in the feed.
@Throws(IOException::class, XmlPullParserException::class)
fun readExamCnt(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, ns, "examCnt")
    val examCnt = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, "examCnt")
    return examCnt
}

// For the tags title and summary, extracts their text values.
@Throws(IOException::class, XmlPullParserException::class)
fun readText(parser: XmlPullParser): String {
    var result = ""
    if (parser.next() == XmlPullParser.TEXT) {
        result = parser.text
        parser.nextTag()
    }
    return result
}

@Throws(XmlPullParserException::class, IOException::class)
fun skip(parser: XmlPullParser) {

    if (parser.eventType != XmlPullParser.START_TAG) {
        throw IllegalStateException()
    }
    var depth = 1
    while (depth != 0) {
        if (parser.name == "items")
            break
        when (parser.next()) {
            XmlPullParser.END_TAG -> depth--
            XmlPullParser.START_TAG -> depth++
            null -> parser.next()
        }
    }
}