package peterlavalle

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

object ISO8061 {
  def day(date: Date = new Date()): String = {
    import java.util.Locale
    val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK)
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
    dateFormat.format(date)
  }
}
