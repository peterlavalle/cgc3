package peterlavalle

import java.io.{File, FileOutputStream}
import java.net.URL
import java.util.zip.ZipFile

class UrlCache(root: File) {

	def zip(url: String): ZipFile = new ZipFile(apply(url))

	def apply(url: String): File =
		this {
			new URL(url)
		}

	def zip(url: URL): ZipFile = new ZipFile(apply(url))

	def apply(url: URL): File = {
		val local: File = root / url.getHost / url.getFile

		if (!local.exists())
			(new FileOutputStream(local.EnsureParent) << url.openStream()).close()

		local
	}
}
