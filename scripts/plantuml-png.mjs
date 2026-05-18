import zlib from "node:zlib";
import fs from "node:fs";
import https from "node:https";

/** Same alphabet as plantuml-encoder / PlantUML JS reference. */
function encode6bit(b) {
  if (b < 10) return String.fromCharCode(48 + b);
  b -= 10;
  if (b < 26) return String.fromCharCode(65 + b);
  b -= 26;
  if (b < 26) return String.fromCharCode(97 + b);
  b -= 26;
  if (b === 0) return "-";
  if (b === 1) return "_";
  return "?";
}

function append3bytes(b1, b2, b3) {
  const c1 = b1 >> 2;
  const c2 = ((b1 & 0x3) << 4) | (b2 >> 4);
  const c3 = ((b2 & 0xf) << 2) | (b3 >> 6);
  const c4 = b3 & 0x3f;
  return (
    encode6bit(c1 & 0x3f) +
    encode6bit(c2 & 0x3f) +
    encode6bit(c3 & 0x3f) +
    encode6bit(c4 & 0x3f)
  );
}

/** Matches plantuml-encoder encode64 branch order on binary buffer bytes. */
function encodePlantuml64(buf) {
  let r = "";
  for (let i = 0; i < buf.length; i += 3) {
    if (i + 2 === buf.length) {
      r += append3bytes(buf[i], buf[i + 1], 0);
    } else if (i + 1 === buf.length) {
      r += append3bytes(buf[i], 0, 0);
    } else {
      r += append3bytes(buf[i], buf[i + 1], buf[i + 2]);
    }
  }
  return r;
}

function plantumlPngUrl(source) {
  const compressed = zlib.deflateRawSync(Buffer.from(source, "utf8"), {
    level: 9,
  });
  const encoded = encodePlantuml64(compressed);
  // PlantUML public server uses /png/<encoded> (no ~1 prefix; see plantuml.com/text-encoding).
  return `https://www.plantuml.com/plantuml/png/${encoded}`;
}

function httpsGetBuffer(url) {
  return new Promise((resolve, reject) => {
    https
      .get(url, (res) => {
        if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
          httpsGetBuffer(res.headers.location).then(resolve).catch(reject);
          return;
        }
        if (res.statusCode !== 200) {
          reject(new Error(`HTTP ${res.statusCode}`));
          res.resume();
          return;
        }
        const chunks = [];
        res.on("data", (c) => chunks.push(c));
        res.on("end", () => resolve(Buffer.concat(chunks)));
      })
      .on("error", reject);
  });
}

function isPng(buf) {
  return (
    buf.length >= 8 &&
    buf[0] === 0x89 &&
    buf[1] === 0x50 &&
    buf[2] === 0x4e &&
    buf[3] === 0x47
  );
}

const [, , inputPath, outputPath] = process.argv;
if (!inputPath || !outputPath) {
  console.error("Usage: node plantuml-png.mjs <input.puml> <output.png>");
  process.exit(1);
}

const puml = fs.readFileSync(inputPath, "utf8");
const buf = await httpsGetBuffer(plantumlPngUrl(puml));

if (!isPng(buf)) {
  console.error("Réponse invalide (pas un PNG).");
  process.exit(1);
}

fs.writeFileSync(outputPath, buf);
console.log("Wrote", outputPath, "(" + buf.length + " bytes)");
