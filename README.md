# The Censor Census Android Client #
part of the [Bowdlerize.co.uk](https://bowdlerize.co.uk) Censor Census Project

## How To Help Bring Transparency To UK Internet Filtering ##

* Install the app from [Google Play](https://play.google.com/store/apps/details?id=uk.bowdlerize)
* URLs are sent to your phone/tablet from the Bowdlerize servers *(URLs are found from social media or user submissions)*
* Your device issues a [HTTP HEAD request](http://developer.android.com/reference/org/apache/http/client/methods/HttpHead.html) and then reports back if it could reach the URL or not
* The Bowdlerize servers retain an MD5 hash of the URL and the censor state **NOT** the URLs themselves
* Eventually we will have a list of all censored URLs allowing people to create browser plugins to create a client side version of [451 Unavailable](http://www.451unavailable.org/)

## Why is this needed? ##

The UK Government is extending its silent censorship of the Internet expanding way beyond abhorant images and moving to block extreme / esoteric material.

This material will become inaccessible via spoofed 404s, time outs and other errors. People will not know they are being censored.

Unlike the [British Board of Film Classification](http://www.bbfc.co.uk/) or other industry bodies that control what can and can't be seen these Internet filters are silent and unaccountable. 