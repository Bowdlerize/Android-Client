# The Censor Census Android Client #
part of the [Bowdlerize.co.uk](https://bowdlerize.co.uk) Censor Census Project, the [Open Rights Group Censorship Monitoring Project](https://wiki.openrightsgroup.org/wiki/ORG_Censorship_Monitoring_Project) feeding into the [Open Observatory of Network Interference](https://ooni.torproject.org/)

## How To Help Bring Transparency To UK Internet Filtering ##

* Install the app from [Google Play](https://play.google.com/store/apps/details?id=uk.bowdlerize)
* URLs are sent to your phone/tablet from the Bowdlerize servers *(URLs are found from social media or user submissions)*
* Your device issues a [HTTP HEAD request](http://developer.android.com/reference/org/apache/http/client/methods/HttpHead.html) and then reports back if it could reach the URL or not
* The Bowdlerize servers retain an MD5 hash of the URL and the censor state **NOT** the URLs themselves
* A full OONI payload can be delivered to the Open Rights Group middleware servers for processing

## Why is this needed? ##

The UK Government is extending its silent censorship of the Internet expanding way beyond abhorant images and moving to block extreme / esoteric material.

This material will become inaccessible via spoofed 404s, time outs and other errors. People will not know they are being censored.

Unlike the [British Board of Film Classification](http://www.bbfc.co.uk/) or other industry bodies that control what can and can't be seen these Internet filters are silent and unaccountable.