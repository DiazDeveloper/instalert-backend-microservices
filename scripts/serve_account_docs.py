"""Local Swagger UI for the Account contract; API requests go through the Gateway."""
import argparse
import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "target" / "swagger-assets"
CONTRACT = ROOT / "services" / "ms-account" / "openapi.json"
HTML = '''<!doctype html><html lang="es"><head><meta charset="utf-8">
<title>InstAlert Account — Swagger UI</title>
<link rel="stylesheet" href="/swagger-ui.css">
<style>body{margin:0}.notice{padding:14px 24px;background:#edf4ff;font-family:Arial}</style>
</head><body><div class="notice">InstAlert — Account · Sprint Review ·
Las solicitudes de esta documentación se envían al API Gateway local (8080).</div>
<div id="swagger-ui"></div><script src="/swagger-ui-bundle.js"></script>
<script>SwaggerUIBundle({url:'/openapi.json',dom_id:'#swagger-ui',deepLinking:true,
validatorUrl:null,persistAuthorization:false,displayRequestDuration:true,
defaultModelsExpandDepth:0});</script></body></html>'''

class Handler(BaseHTTPRequestHandler):
    def send(self, status, data, content_type):
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(data)

    def do_GET(self):
        if self.path.split("?")[0].startswith("/api/v1/"):
            return self.proxy()
        if self.path in ("/", "/swagger-ui/index.html"):
            return self.send(200, HTML.encode(), "text/html; charset=utf-8")
        if self.path == "/openapi.json":
            spec = json.loads(CONTRACT.read_text(encoding="utf-8"))
            spec["servers"] = [{"url": "/api/v1", "description": "Gateway local mediante proxy de documentación"}]
            return self.send(200, json.dumps(spec).encode(), "application/json")
        assets = {"/swagger-ui.css": "text/css", "/swagger-ui-bundle.js": "text/javascript"}
        if self.path in assets:
            file = ASSETS / self.path[1:]
            if file.is_file():
                return self.send(200, file.read_bytes(), assets[self.path])
        self.send(404, b"Not found", "text/plain")

    def proxy(self):
        # Proxy only declared Account operations to a fixed local Gateway.
        import re
        path = self.path.split("?")[0]
        spec = json.loads(CONTRACT.read_text(encoding="utf-8"))
        allowed = any(self.command.lower() in operations and
                      re.fullmatch("/api/v1" + re.sub(r"\{[^}]+\}", "[^/]+", template), path)
                      for template, operations in spec["paths"].items())
        if not allowed or ".." in path or "%" in path:
            return self.send(404, b"Unknown Account operation", "text/plain")
        length = int(self.headers.get("Content-Length", 0))
        if length > 65536:
            return self.send(413, b"Request too large", "text/plain")
        body = self.rfile.read(length) if length else None
        headers = {key: self.headers[key] for key in ("Authorization", "Content-Type", "Accept") if key in self.headers}
        request = Request("http://127.0.0.1:8080" + self.path, data=body, headers=headers, method=self.command)
        try:
            with urlopen(request, timeout=15) as response:
                self.send(response.status, response.read(), response.headers.get("Content-Type", "application/json"))
        except HTTPError as error:
            self.send(error.code, error.read(), error.headers.get("Content-Type", "application/json"))
        except URLError:
            self.send(502, b'{"error":"Local Gateway unavailable on port 8080"}', "application/json")

    do_POST = proxy
    do_PUT = proxy
    do_DELETE = proxy

    def log_message(self, format, *args):
        # Never log bodies, credentials or Authorization headers.
        print(format % args, flush=True)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default=8090)
    args = parser.parse_args()
    if not all((ASSETS / name).is_file() for name in ("swagger-ui.css", "swagger-ui-bundle.js")):
        raise SystemExit("Missing Swagger UI assets in target/swagger-assets. See docs/SWAGGER_LOCAL.md.")
    print(f"Account Swagger UI: http://localhost:{args.port}/swagger-ui/index.html", flush=True)
    ThreadingHTTPServer(("127.0.0.1", args.port), Handler).serve_forever()
