FROM alpine:latest
RUN apk add --no-cache python3
WORKDIR /app
COPY . .
EXPOSE 8088
CMD ["python3", "-m", "http.server", "8088"]
