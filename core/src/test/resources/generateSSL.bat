keytool -genkeypair -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass password -keypass password -keysize 2048 -dname "CN=Andrew Trimble, OU=Engineering, O=Archarithms Inc., L=Denver, ST=CO, C=US"
keytool -importkeystore -srckeystore keystore.jks -destkeystore keystore.p12 -srcstoretype jks -deststoretype pkcs12 -srcstorepass password -deststorepass password
openssl pkcs12 -nocerts -in keystore.p12 -out keystore.pem -passin pass:password -passout pass:password
openssl pkcs12 -clcerts -nokeys -in keystore.p12 -out certs.pem -passin pass:password -passout pass:password
openssl rsa -in keystore.pem -out keystore.unencrypted.pem -passin pass:password
openssl pkcs8 -topk8 -in keystore.unencrypted.pem -out keystore.pkcs8.pem -passin pass:password -passout pass:password
rm keystore.unencrypted.pem
