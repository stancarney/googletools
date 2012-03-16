    @Grapes([
    @Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.5.1'),
    @Grab(group = 'org.ccil.cowan.tagsoup', module = 'tagsoup', version = '0.9.7')
    ])

    term = args[0]
    pattern = args[1]
    google = args[2] //"www.google.ca" //incase you wanted to use country specific googles. i.e. www.google.co.uk

    //this is pulled from the PREF cookie.
    googlePrefCookieValue = "ID=5009ec249fd04464:U=fcc4d3404369c847:FF=0:LD=en:NR=100:TM=1317840904:LM=1317840920:SG=2:S=zOhv6X9cMQ9b3CRN"

    http = new groovyx.net.http.HTTPBuilder("http://${google}")

    cookie = new org.apache.http.impl.cookie.BasicClientCookie('PREF', googlePrefCookieValue)
    cookie["domain"] = google
    cookie["path"] = '/'
    http.client.cookieStore.addCookie cookie

    position = 0
    http.request(groovyx.net.http.Method.GET, groovyx.net.http.ContentType.TEXT) { req ->
      uri.path = '/search'
      uri.query = [q: term]
      headers.'User-Agent' = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0)"

      response.success = { resp, reader ->

        assert resp.statusLine.statusCode == 200
        html = new XmlSlurper(new org.ccil.cowan.tagsoup.Parser()).parse(reader)

        results = html.body.'**'.findAll { e1 ->
          e1.name() == 'li' && e1.'@class' == 'g' && e1.@id == ''
        }

        for (line in results) {
          position++

          match = line.'**'.find { e2 -> e2.@class.text().startsWith('l') && e2.name() == 'a' && e2.'@href'.text() =~ pattern}?.@href
          if (match) {
            println match
            println position
            break
          }
        }
      }
    }
