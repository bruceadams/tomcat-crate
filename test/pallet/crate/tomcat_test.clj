(ns pallet.crate.tomcat-test
  (:refer-clojure :exclude [alias])
  (:use [pallet.crate.tomcat] :reload-all)
  (:require
    [pallet.crate.tomcat :as tc]
    [pallet.target :as target]
    [pallet.resource.remote-file :as remote-file]
    [pallet.template :as template]
    [net.cgrand.enlive-html :as enlive-html]
    [pallet.enlive :as enlive])
  (:use
   clojure.test
   pallet.test-utils
   [pallet.resource.package :only [package package-manager]]
   [pallet.resource :only [build-resources]]
   [pallet.stevedore :only [script]]
   [pallet.utils :only [cmd-join]]
   [pallet.core :only [defnode]]))

(deftest tomcat-test
  (is (= "debconf-set-selections <<EOF\ndebconf debconf/frontend select noninteractive\ndebconf debconf/frontend seen false\nEOF\naptitude install -y  tomcat6\n"
         (build-resources [] (tomcat)))))

(deftest classname-for-test
  (let [m {:a "a" :b "b"}]
    (is (= "a" (classname-for :a m)))
    (is (= "b" (classname-for :b m)))
    (is (= "c" (classname-for "c" m)))))

(deftest tomcat-deploy-test
  (is (= (remote-file/remote-file*
          "/var/lib/tomcat6/webapps/ROOT.war"
          :remote-file "file.war"
          :owner "tomcat6" :group "tomcat6" :mode "600")
         (build-resources [] (deploy "file.war" nil)))))

(deftest tomcat-undeploy-all-test
  (is (= "rm -r -f /var/lib/tomcat6/webapps/*\n"
        (build-resources [] (undeploy-all)))))

(deftest tomcat-undeploy-test
  (is (= "rm -r -f /var/lib/tomcat6/webapps/ROOT\nrm -f /var/lib/tomcat6/webapps/ROOT.war\nrm -r -f /var/lib/tomcat6/webapps/app\nrm -f /var/lib/tomcat6/webapps/app.war\nrm -r -f /var/lib/tomcat6/webapps/foo\nrm -f /var/lib/tomcat6/webapps/foo.war\n"
        (build-resources [] (undeploy nil :app "foo"))))
  (is (= "" (build-resources [] (undeploy)))))

(deftest tomcat-policy-test
  (is (= (remote-file/remote-file*
          "/etc/tomcat6/policy.d/100hudson.policy"
          :content "grant codeBase \"file:${catalina.base}/webapps/hudson/-\" {\n  permission java.lang.RuntimePermission \"getAttribute\";\n};"
          :literal true)
         (build-resources []
          (policy
           100 "hudson"
           {"file:${catalina.base}/webapps/hudson/-"
            ["permission java.lang.RuntimePermission \"getAttribute\""]})))))

(deftest tomcat-blanket-policy-test
  (is (= (remote-file/remote-file*
          "/etc/tomcat6/policy.d/100hudson.policy"
          :content "grant  {\n  permission java.lang.RuntimePermission \"getAttribute\";\n};"
          :literal true)
         (build-resources []
          (policy
           100 "hudson"
           {nil ["permission java.lang.RuntimePermission \"getAttribute\""]})))))

(deftest tomcat-application-conf-test
  (is (= (remote-file/remote-file*
          "/etc/tomcat6/Catalina/localhost/hudson.xml"
          :content "<?xml version='1.0' encoding='utf-8'?>\n<Context docBase=\"/srv/hudson/hudson.war\">\n<Environment name=\"HUDSON_HOME\"/>\n</Context>"
          :literal true)
         (build-resources []
          (application-conf
           "hudson"
           "<?xml version='1.0' encoding='utf-8'?>
<Context docBase=\"/srv/hudson/hudson.war\">
<Environment name=\"HUDSON_HOME\"/>
</Context>")))))

(deftest tomcat-user-test
  (is (= "<decl version=\"1.1\"/><tomcat-users><role rolename=\"r1\"/><role rolename=\"r2\"/><user username=\"u2\" password=\"p2\" roles=\"r1,r2\"/><user username=\"u1\" password=\"p1\" roles=\"r1\"/></tomcat-users>\n"
         (build-resources []
          (user :role "r1" "u1" {:password "p1" :roles ["r1"]})
          (user :role "r2" "u2" {:password "p2" :roles ["r1","r2"]})))))

(deftest extract-member-keys-test
  (are [#{:m} #{:c} [:m 1 :c 2 :c 3 :d 1]] =
       (extract-member-keys [:members [:m] :collections [:c] :m 1 :c 2 :c 3 :d 1]))
  (are [#{:m} #{} [:m 1 :c 2 :c 3 :d 1]] =
       (extract-member-keys [:members [:m] :m 1 :c 2 :c 3 :d 1]))
  (are [#{} #{} [:m 1 :c 2 :c 3 :d 1]] =
       (extract-member-keys [:m 1 :c 2 :c 3 :d 1])))

(deftest extract-nested-maps-test
  (is (= {:d 1
          :c [{::tc/pallet-type :c :v 2} {::tc/pallet-type :c :v 3}]
          :m {::tc/pallet-type :m :v 1}}
         (extract-nested-maps
          [#{:m} #{:c}
           [{::tc/pallet-type :m :v 1}
            {::tc/pallet-type :c :v 2}
            {::tc/pallet-type :c :v 3}
            :d 1]]))))

(deftest pallet-type-test
  (= {::tc/pallet-type :t :u {::tc/pallet-type :u :v 1} :a 1}
     (pallet-type :t :member [:u] [:a 1 {::tc/pallet-type :u :v 1}]))
  (= {::tc/pallet-type :t :a 1}
     (pallet-type :t [:a 1])))

(deftest alias-test
  (is (= {::tc/pallet-type ::tc/alias :name "fred"}
         (alias "fred"))))

(deftest connector-test
  (is (= {::tc/pallet-type ::tc/connector :port 8443}
         (connector :port 8443))))

(deftest ssl-jsee-connector-test
  (is (= {::tc/pallet-type ::tc/connector
          :maxThreads 150 :protocol "HTTP/1.1" :scheme "https"
          :keystorePass "changeit" :sslProtocol "TLS" :clientAuth "false"
          :SSLEnabled "true" :port 8442 :secure "true"
          :keystoreFile "${user.home}/.keystore"}
         (ssl-jsee-connector :port 8442))))

(deftest ssl-apr-connector-test
  (is (= {::tc/pallet-type ::tc/connector
          :maxThreads 150 :protocol "HTTP/1.1" :scheme "https"
          :sslProtocol "TLSv1" :clientAuth "optional" :SSLEnabled "true"
          :port 8442 :secure "true"
          :SSLCertificateKeyFile= "/usr/local/ssl/server.pem"
          :SSLCertificateFile "/usr/local/ssl/server.crt"}
         (ssl-apr-connector :port 8442))))

(deftest listener-test
  (is (= {::tc/pallet-type ::tc/listener :className "org.apache.catalina.core.JasperListener"}
         (listener :jasper))))

(deftest global-resources-test
  (is (= {::tc/pallet-type ::tc/global-resources}
         (global-resources))))

(deftest host-test
  (is (= {::tc/pallet-type ::tc/host :name "localhost" :appBase "webapps"
          ::tc/valve [{::tc/pallet-type ::tc/valve
                    :className "org.apache.catalina.valves.RequestDumperValve"}]
          ::tc/alias [{::tc/pallet-type ::tc/alias :name "www.domain.com"}]}
         (host "localhost" "webapps"
           (alias "www.domain.com")
             (valve :request-dumper)))))

(deftest service-test
  (is (= {::tc/pallet-type ::tc/service
          ::tc/connector
          [{::tc/pallet-type ::tc/connector :redirectPort "8443" :connectionTimeout "20000"
            :port "8080" :protocol "HTTP/1.1"}]
          ::tc/engine {::tc/pallet-type ::tc/engine :defaultHost "host" :name "catalina"
                       ::tc/valve
                       [{::tc/pallet-type ::tc/valve
                         :className "org.apache.catalina.valves.RequestDumperValve"}]}}
         (service
          (engine "catalina" "host"
                  (valve :request-dumper))
          (connector :port "8080" :protocol "HTTP/1.1"
                     :connectionTimeout "20000"
                     :redirectPort "8443")))))

(deftest server-test
  (is (= {::tc/pallet-type ::tc/server :port "123", :shutdown "SHUTDOWNx"
          ::tc/global-resources {::tc/pallet-type ::tc/global-resources}
          ::tc/listener
          [{::tc/pallet-type ::tc/listener :className "org.apache"}
           {::tc/pallet-type ::tc/listener
            :className "org.apache.catalina.core.JasperListener"}]}
         (server
          :port "123" :shutdown "SHUTDOWNx"
          (listener "org.apache")
          (listener :jasper)
          (global-resources))))
  (is (= {::tc/pallet-type ::tc/server :port "123" :shutdown "SHUTDOWNx"
          ::tc/service
          [{::tc/pallet-type ::tc/service
            ::tc/connector
            [{::tc/pallet-type ::tc/connector :redirectPort "8443"
              :connectionTimeout "20000" :port "8080" :protocol "HTTP/1.1"}]
            ::tc/engine
            {::tc/pallet-type ::tc/engine :defaultHost "host" :name "catalina"
             ::tc/host [{::tc/pallet-type ::tc/host, :name "localhost", :appBase "webapps"}]
             ::tc/valve [{::tc/pallet-type ::tc/valve :className
                       "org.apache.catalina.valves.RequestDumperValve"}]}}]
          ::tc/global-resources {::tc/pallet-type ::tc/global-resources}}
         (server
          :port "123" :shutdown "SHUTDOWNx"
          (global-resources)
          (service
           (engine "catalina" "host"
                   (valve :request-dumper)
                   (host "localhost" "webapps"))
           (connector :port "8080" :protocol "HTTP/1.1"
                      :connectionTimeout "20000"
                      :redirectPort "8443"))))))


(deftest tomcat-server-xml-test
  (defnode test-node [:ubuntu])
  (is (= "<?xml version='1.0' encoding='utf-8'?>\n<Server shutdown=\"SHUTDOWNx\" port=\"123\">\n  <Listener className=\"org.apache.catalina.core.JasperListener\"></Listener>\n  <Listener className=\"org.apache.catalina.mbeans.ServerLifecycleListener\"></Listener>\n  <Listener className=\"org.apache.catalina.mbeans.GlobalResourcesLifecycleListener\"></Listener>\n  <GlobalNamingResources>\n    <Resource pathname=\"conf/tomcat-users.xml\" factory=\"org.apache.catalina.users.MemoryUserDatabaseFactory\" description=\"User database that can be updated and saved\" type=\"org.apache.catalina.UserDatabase\" auth=\"Container\" name=\"UserDatabase\"></Resource>\n  </GlobalNamingResources>\n\n  <Service name=\"Catalina\">\n    <Connector URIEncoding=\"UTF-8\" redirectPort=\"8443\" connectionTimeout=\"20000\" protocol=\"HTTP/1.1\" port=\"8080\"></Connector>\n    <Engine defaultHost=\"localhost\" name=\"Catalina\">\n      <Realm resourceName=\"UserDatabase\" className=\"org.apache.catalina.realm.UserDatabaseRealm\"></Realm>\n\n      <Host xmlNamespaceAware=\"false\" xmlValidation=\"false\" deployOnStartup=\"true\" autoDeploy=\"true\" unpackWARs=\"true\" appBase=\"webapps\" name=\"localhost\">\n\n\t<Valve resolveHosts=\"false\" pattern=\"common\" suffix=\".log\" prefix=\"localhost_access.\" directory=\"logs\" className=\"org.apache.catalina.valves.AccessLogValve\"></Valve>\n      </Host>\n    </Engine>\n  </Service>\n</Server>"
         (apply str (tomcat-server-xml
                      test-node
                      (server :port "123" :shutdown "SHUTDOWNx"))))
      "Listener, GlobalNaminResources and Service should be taken from template")
  (is (= "<?xml version='1.0' encoding='utf-8'?>\n<Server shutdown=\"SHUTDOWNx\" port=\"123\"><GlobalNamingResources></GlobalNamingResources><Listener className=\"org.apache\"></Listener><Listener className=\"org.apache.catalina.core.JasperListener\"></Listener>\n  \n  \n  \n  \n\n  <Service name=\"Catalina\">\n    <Connector URIEncoding=\"UTF-8\" redirectPort=\"8443\" connectionTimeout=\"20000\" protocol=\"HTTP/1.1\" port=\"8080\"></Connector>\n    <Engine defaultHost=\"localhost\" name=\"Catalina\">\n      <Realm resourceName=\"UserDatabase\" className=\"org.apache.catalina.realm.UserDatabaseRealm\"></Realm>\n\n      <Host xmlNamespaceAware=\"false\" xmlValidation=\"false\" deployOnStartup=\"true\" autoDeploy=\"true\" unpackWARs=\"true\" appBase=\"webapps\" name=\"localhost\">\n\n\t<Valve resolveHosts=\"false\" pattern=\"common\" suffix=\".log\" prefix=\"localhost_access.\" directory=\"logs\" className=\"org.apache.catalina.valves.AccessLogValve\"></Valve>\n      </Host>\n    </Engine>\n  </Service>\n</Server>"
         (apply str
                (tomcat-server-xml
                 test-node
                 (server
                  :port "123" :shutdown "SHUTDOWNx"
                  (listener "org.apache")
                  (listener :jasper)
                  (global-resources)))))
      "Listener, GlobalNamingResources and Service should be taken from args")

  (is (= "<?xml version='1.0' encoding='utf-8'?>\n<Server shutdown=\"SHUTDOWNx\" port=\"123\"><GlobalNamingResources><Transaction factory=\"some.transaction.class\"></Transaction><resource></resource><Environment name=\"name\" value=\"1\" type=\"java.lang.Integer\"></Environment></GlobalNamingResources>\n  <Listener className=\"org.apache.catalina.core.JasperListener\"></Listener>\n  <Listener className=\"org.apache.catalina.mbeans.ServerLifecycleListener\"></Listener>\n  <Listener className=\"org.apache.catalina.mbeans.GlobalResourcesLifecycleListener\"></Listener>\n  \n\n  <Service name=\"Catalina\">\n    <Connector URIEncoding=\"UTF-8\" redirectPort=\"8443\" connectionTimeout=\"20000\" protocol=\"HTTP/1.1\" port=\"8080\"></Connector>\n    <Engine defaultHost=\"localhost\" name=\"Catalina\">\n      <Realm resourceName=\"UserDatabase\" className=\"org.apache.catalina.realm.UserDatabaseRealm\"></Realm>\n\n      <Host xmlNamespaceAware=\"false\" xmlValidation=\"false\" deployOnStartup=\"true\" autoDeploy=\"true\" unpackWARs=\"true\" appBase=\"webapps\" name=\"localhost\">\n\n\t<Valve resolveHosts=\"false\" pattern=\"common\" suffix=\".log\" prefix=\"localhost_access.\" directory=\"logs\" className=\"org.apache.catalina.valves.AccessLogValve\"></Valve>\n      </Host>\n    </Engine>\n  </Service>\n</Server>"
         (apply str
                (tomcat-server-xml
                 test-node
                 (server
                  :port "123" :shutdown "SHUTDOWNx"
                  (global-resources
                   (environment "name" 1 Integer)
                   (resource "jdbc/mydb" :sql-data-source :description "my db")
                   (transaction "some.transaction.class"))))))
      "Listener, GlobalNamingResources and Service should be taken from args"))

(deftest server-configuration-test
  (defnode a [])
  (target/with-target nil {:tag :a :image [:ubuntu]}
    (is (= (remote-file/remote-file*
            "/var/lib/tomcat6/conf/server.xml"
            :content "<?xml version='1.0' encoding='utf-8'?>\n<Server shutdown=\"SHUTDOWNx\" port=\"123\"><GlobalNamingResources></GlobalNamingResources><Listener className=\"\"></Listener>\n  \n  \n  \n  \n\n  <Service name=\"Catalina\">\n    <Connector URIEncoding=\"UTF-8\" redirectPort=\"8443\" connectionTimeout=\"20000\" protocol=\"HTTP/1.1\" port=\"80\"></Connector>\n    <Engine defaultHost=\"host\" name=\"catalina\"><Valve className=\"org.apache.catalina.valves.RequestDumperValve\"></Valve>\n      <Realm resourceName=\"UserDatabase\" className=\"org.apache.catalina.realm.UserDatabaseRealm\"></Realm>\n\n      <Host xmlNamespaceAware=\"false\" xmlValidation=\"false\" deployOnStartup=\"true\" autoDeploy=\"true\" unpackWARs=\"true\" appBase=\"webapps\" name=\"localhost\">\n\n\t\n      </Host>\n    </Engine>\n  </Service>\n</Server>")
           (build-resources []
                            (server-configuration
                             (server
                              :port "123" :shutdown "SHUTDOWNx"
                              (listener :global-resources-lifecycle)
                              (global-resources)
                              (service
                               (engine "catalina" "host"
                                       (valve :request-dumper))
                               (connector :port "80" :protocol "HTTP/1.1"
                                          :connectionTimeout "20000"
                                          :redirectPort "8443")))))))
    (is (= (remote-file/remote-file*
            "/var/lib/tomcat6/conf/server.xml"
            :content "<?xml version='1.0' encoding='utf-8'?>\n<Server shutdown=\"SHUTDOWN\" port=\"8005\">\n  <Listener className=\"org.apache.catalina.core.JasperListener\"></Listener>\n  <Listener className=\"org.apache.catalina.mbeans.ServerLifecycleListener\"></Listener>\n  <Listener className=\"org.apache.catalina.mbeans.GlobalResourcesLifecycleListener\"></Listener>\n  <GlobalNamingResources>\n    <Resource pathname=\"conf/tomcat-users.xml\" factory=\"org.apache.catalina.users.MemoryUserDatabaseFactory\" description=\"User database that can be updated and saved\" type=\"org.apache.catalina.UserDatabase\" auth=\"Container\" name=\"UserDatabase\"></Resource>\n  </GlobalNamingResources>\n\n  <Service name=\"Catalina\">\n    <Connector URIEncoding=\"UTF-8\" redirectPort=\"8443\" connectionTimeout=\"20000\" protocol=\"HTTP/1.1\" port=\"8080\"></Connector>\n    <Engine defaultHost=\"localhost\" name=\"Catalina\">\n      <Realm resourceName=\"UserDatabase\" className=\"org.apache.catalina.realm.UserDatabaseRealm\"></Realm>\n\n      <Host xmlNamespaceAware=\"false\" xmlValidation=\"false\" deployOnStartup=\"true\" autoDeploy=\"true\" unpackWARs=\"true\" appBase=\"webapps\" name=\"localhost\">\n\n\t<Valve resolveHosts=\"false\" pattern=\"common\" suffix=\".log\" prefix=\"localhost_access.\" directory=\"logs\" className=\"org.apache.catalina.valves.AccessLogValve\"></Valve>\n      </Host>\n    </Engine>\n  </Service>\n</Server>")
           (build-resources []
                            (server-configuration
                             (server)))))))

