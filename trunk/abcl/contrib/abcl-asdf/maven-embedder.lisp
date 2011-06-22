;;; Use the Aether system in a default maven distribution to download
;;; and install dependencies.
;;;
;;; https://docs.sonatype.org/display/AETHER/Home
;;;

(in-package :abcl-asdf)

(require :abcl-contrib)
(require :jss)

#| 
Test:

(resolve "org.slf4j" "slf4j-api" "1.6.1")
|#

(defvar *mavens* '("/opt/local/bin/mvn3" "mvn3" "mvn"))

(defun find-mvn () 
  (dolist (mvn-path *mavens*)
    (let ((mvn 
           (handler-case 
               (truename (read-line (sys::process-output 
                                     (sys::run-program "which" `(,mvn-path)))))
             ('end-of-file () 
               nil))))
      (when mvn
        (return-from find-mvn mvn)))))

(defun find-mvn-libs ()
  (let ((mvn (find-mvn)))
    (unless mvn
      (warn "Failed to find Maven3 libraries.")
      (return-from find-mvn-libs))
    (truename (make-pathname 
               :defaults (merge-pathnames "../lib/" mvn)
               :name nil :type nil))))

(defparameter *mvn-libs-directory*
  nil
  "Location of 'maven-core-3.<m>.<p>.jar', 'maven-embedder-3.<m>.<p>.jar' etc.")

(defun mvn-version ()
  (let ((line
         (read-line (sys::process-output (sys::run-program 
                                          (namestring (find-mvn)) '("-version")))))
        (prefix "Apache Maven "))

    (unless (eql (search prefix line) 0)
      (return-from mvn-version nil))
    (let ((version (subseq line (length prefix))))
      version)))

;;; XXX will break with release of Maven 3.1.x
(defun ensure-mvn-version ()
  "Return t if Maven version is 3.0.3 or greater."
  (let ((version-string (mvn-version)))
    (and (search "3.0" version-string)
         (>= (parse-integer (subseq version-string 
                                    4 (search " (" version-string)))
             3))))

(defparameter *init* nil)

(defun init ()
  (unless *mvn-libs-directory*
    (setf *mvn-libs-directory* (find-mvn-libs)))
  (unless (probe-file *mvn-libs-directory*)
    (error "You must download maven-3.0.3 from http://maven.apache.org/download.html, then set ABCL-ASDF:*MVN-DIRECTORY* appropiately."))
  (unless (ensure-mvn-version)
    (error "We need maven-3.0.3 or later."))
  (jss:add-directory-jars-to-class-path *mvn-libs-directory* nil)
  (setf *init* t))

(defun repository-system ()
  (unless *init* (init))
  (let ((locator 
         (java:jnew "org.apache.maven.repository.internal.DefaultServiceLocator"))
        (wagon-class 
         (java:jclass "org.sonatype.aether.connector.wagon.WagonProvider"))
        (wagon-provider 
         (jss:find-java-class "LightweightHttpWagon"))
        (repository-connector-factory-class
         (java:jclass "org.sonatype.aether.connector.wagon.WagonRepositoryConnector"))
        (wagon-repository-connector-factory-class
         (java:jclass "org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory"))
        (repository-system-class
         (java:jclass "org.sonatype.aether.RepositorySystem")))
    (#"setService" locator wagon-class wagon-provider)
    (#"addService" locator 
                   repository-connector-factory-class
                   wagon-repository-connector-factory-class)
    (#"getService" locator repository-system-class)))

#|
private static RepositorySystem newRepositorySystem()
{
  DefaultServiceLocator locator = new DefaultServiceLocator();
  locator.setServices( WagonProvider.class, new ManualWagonProvider() );
  locator.addService( RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class );

  return locator.getService( RepositorySystem.class );
}
|#

(defun new-session (repository-system)
  (let ((session 
         (java:jnew (jss:find-java-class "MavenRepositorySystemSession")))
        (local-repository 
         (java:jnew (jss:find-java-class "LocalRepository")
                  (namestring (merge-pathnames ".m2/repository/"
                                               (user-homedir-pathname))))))
    (#"setLocalRepositoryManager" 
     session
     (#"newLocalRepositoryManager" repository-system local-repository))))

#|
private static RepositorySystemSession newSession( RepositorySystem system )
{
  MavenRepositorySystemSession session = new MavenRepositorySystemSession();

  LocalRepository localRepo = new LocalRepository( "target/local-repo" );
  session.setLocalRepositoryManager( system.newLocalRepositoryManager( localRepo ) );
  
  return session;
}
|#

(defun resolve (group-id artifact-id version)
  (unless *init* (init))
  (let* ((system 
          (repository-system))
         (session 
          (new-session system))
         (artifact
          (java:jnew (jss:find-java-class "aether.util.artifact.DefaultArtifact")
                     (format nil "~A:~A:~A"
                             group-id artifact-id version)))
         (dependency 
          (java:jnew (jss:find-java-class "aether.graph.Dependency")
                     artifact "compile"))
         (central
          (java:jnew (jss:find-java-class "RemoteRepository")
                     "central" "default" 
                     "http://repo1.maven.org/maven2/"))
         (collect-request (java:jnew (jss:find-java-class "CollectRequest"))))
    (#"setRoot" collect-request dependency)
    (#"addRepository" collect-request central)
    (let* ((node 
            (#"getRoot" (#"collectDependencies" system session collect-request)))
           (dependency-request 
            (java:jnew (jss:find-java-class "DependencyRequest")
                       node java:+null+))
           (nlg 
            (java:jnew (jss:find-java-class "PreorderNodeListGenerator"))))
      (#"resolveDependencies" system session dependency-request)
      (#"accept" node nlg)
      (#"getClassPath" nlg))))

#|
public static void main( String[] args )
  throws Exception
{
  RepositorySystem repoSystem = newRepositorySystem();

  RepositorySystemSession session = newSession( repoSystem );

  Dependency dependency =
    new Dependency( new DefaultArtifact( "org.apache.maven:maven-profile:2.2.1" ), "compile" );
  RemoteRepository central = new RemoteRepository( "central", "default", "http://repo1.maven.org/maven2/" );

  CollectRequest collectRequest = new CollectRequest();
  collectRequest.setRoot( dependency );
  collectRequest.addRepository( central );
  DependencyNode node = repoSystem.collectDependencies( session, collectRequest ).getRoot();

  DependencyRequest dependencyRequest = new DependencyRequest( node, null );

  repoSystem.resolveDependencies( session, dependencyRequest  );

  PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
  node.accept( nlg );
  System.out.println( nlg.getClassPath() );
}
|#

