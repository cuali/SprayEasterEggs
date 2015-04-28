package reactive.api

import java.security.{ KeyStore, SecureRandom }
import javax.net.ssl.{ KeyManagerFactory, SSLContext, TrustManagerFactory }
import spray.io.ServerSSLEngineProvider

// Update your jre/lib/security jars with the ones from http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
// will be effective only if 'spray.can.server.ssl-encryption' is defined as 'on'
trait ReactiveSecurityConfig {
  implicit lazy val sslContext: SSLContext = {
    val keyStoreResource = "/eggs-keystore.jks"
    val keyStorePassword = "Spray Easter Eggs"
    val keyStore = KeyStore.getInstance("jks")
    keyStore.load(getClass.getResourceAsStream(keyStoreResource), keyStorePassword.toCharArray)
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, keyStorePassword.toCharArray)
    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    context
  }
  implicit lazy val sslEngineProvider: ServerSSLEngineProvider = {
    ServerSSLEngineProvider { engine =>
      engine.setEnabledCipherSuites(engine.getSupportedCipherSuites)
      //engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA"))
      engine.setEnabledProtocols(Array("SSLv3", "TLSv1.2"))
      engine
    }
  }
}
// check out how to prepare a self-signed certificcate in your keystore at http://www.startux.de/index.php/java/44-dealing-with-java-keystoresyvComment44
// keytool -genkeypair -keyalg rsa -dname "CN=Alain Béarez, OU=TI, O=cuali, L=Fortaleza, ST=Ceará, C=BR" -keystore eggs-keystore.jks -storepass "Spray Easter Eggs" -alias "Spray Easter Eggs"
// keytool -certreq -alias "Spray Easter Eggs" -keystore eggs-keystore.jks -storepass "Spray Easter Eggs" -file eggs.csr
// keytool -gencert -infile eggs.csr -outfile eggs.cert -alias "Spray Easter Eggs" -keystore eggs-keystore.jks -storepass "Spray Easter Eggs" -validity 7777
// keytool -importcert -file eggs.cert -alias "Spray Easter Eggs" -keystore eggs-keystore.jks -storepass "Spray Easter Eggs"
