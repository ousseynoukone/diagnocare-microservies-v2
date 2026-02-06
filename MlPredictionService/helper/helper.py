import urllib.request
import urllib.error

def check_eureka_server(app, EUREKA_SERVICE_URL, EUREKA_SERVICE_PATH):
    url = f"{EUREKA_SERVICE_URL.rstrip('/')}/{EUREKA_SERVICE_PATH.lstrip('/')}"
    try:
        with urllib.request.urlopen(url, timeout=3) as response:
            app.logger.info("Eureka server reachable: %s (status %s)", url, response.status)
    except urllib.error.HTTPError as exc:
        app.logger.warning("Eureka server responded with %s for %s", exc.code, url)
    except Exception as exc:
        app.logger.error("Eureka server not reachable: %s (%s)", url, exc)