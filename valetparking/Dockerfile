# Use the official Tomcat 10 base image
FROM tomcat:10.1-jdk17

# Remove the default webapps to avoid conflicts
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file to the webapps directory
COPY target/valetparking-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Expose the Tomcat port
EXPOSE 9000

# Start Tomcat
CMD ["catalina.sh", "run"]
