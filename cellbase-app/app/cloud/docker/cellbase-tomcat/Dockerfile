FROM tomcat:8.0-alpine

LABEL maintainer="julie.sullivan@gmail.com"

COPY cellbase.war /usr/local/tomcat/webapps/

EXPOSE 8080

CMD ["catalina.sh", "run"]