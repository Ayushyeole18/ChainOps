import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import fs from 'fs';
import path from 'path';
import JSZip from 'jszip';
import { defineConfig, Plugin } from 'vite';

function chainOpsApiPlugin(): Plugin {
  return {
    name: 'chainops-api-plugin',
    configureServer(server) {
      // Endpoint to download the entire ChainOps project as a zip file
      server.middlewares.use('/api/download-zip', async (req, res) => {
        try {
          const zip = new JSZip();
          const baseDir = path.resolve(process.cwd(), 'ChainOps');

          function addDirectoryToZip(dirPath: string, zipFolder: JSZip) {
            const items = fs.readdirSync(dirPath);
            for (const item of items) {
              const fullPath = path.join(dirPath, item);
              const stat = fs.statSync(fullPath);
              if (stat.isDirectory()) {
                const subFolder = zipFolder.folder(item);
                if (subFolder) {
                  addDirectoryToZip(fullPath, subFolder);
                }
              } else {
                const fileData = fs.readFileSync(fullPath);
                zipFolder.file(item, fileData);
              }
            }
          }

          const rootFolder = zip.folder('ChainOps');
          if (rootFolder && fs.existsSync(baseDir)) {
            addDirectoryToZip(baseDir, rootFolder);
          }

          const content = await zip.generateAsync({
            type: 'nodebuffer',
            compression: 'DEFLATE',
            compressionOptions: { level: 9 },
          });

          res.setHeader('Content-Type', 'application/zip');
          res.setHeader(
            'Content-Disposition',
            'attachment; filename="ChainOps-SupplyChainSystem.zip"',
          );
          res.setHeader('Content-Length', content.length);
          res.end(content);
        } catch (err: any) {
          res.statusCode = 500;
          res.end(JSON.stringify({ error: err.message }));
        }
      });

      // Endpoint to get all project files for explorer
      server.middlewares.use('/api/project-tree', (req, res) => {
        try {
          const baseDir = path.resolve(process.cwd(), 'ChainOps');

          function buildTree(dirPath: string, relPath = ''): any[] {
            if (!fs.existsSync(dirPath)) return [];
            const items = fs.readdirSync(dirPath);
            const nodes: any[] = [];

            for (const item of items) {
              const itemRel = relPath ? `${relPath}/${item}` : item;
              const fullPath = path.join(dirPath, item);
              const stat = fs.statSync(fullPath);

              if (stat.isDirectory()) {
                nodes.push({
                  name: item,
                  path: itemRel,
                  type: 'folder',
                  children: buildTree(fullPath, itemRel),
                });
              } else {
                let language = 'plaintext';
                if (item.endsWith('.java')) language = 'java';
                else if (item.endsWith('.xml') || item.endsWith('.fxml')) language = 'xml';
                else if (item.endsWith('.css')) language = 'css';
                else if (item.endsWith('.sql')) language = 'sql';
                else if (item.endsWith('.md')) language = 'markdown';
                else if (item.endsWith('.properties')) language = 'properties';
                else if (item.endsWith('.sh') || item.endsWith('.bat')) language = 'shell';

                const content = fs.readFileSync(fullPath, 'utf-8');
                nodes.push({
                  name: item,
                  path: itemRel,
                  type: 'file',
                  language,
                  size: stat.size,
                  content,
                });
              }
            }

            // Sort: folders first, then files alphabetically
            return nodes.sort((a, b) => {
              if (a.type === b.type) return a.name.localeCompare(b.name);
              return a.type === 'folder' ? -1 : 1;
            });
          }

          const tree = buildTree(baseDir);
          res.setHeader('Content-Type', 'application/json');
          res.end(JSON.stringify(tree));
        } catch (err: any) {
          res.statusCode = 500;
          res.end(JSON.stringify({ error: err.message }));
        }
      });
    },
  };
}

export default defineConfig(() => {
  return {
    plugins: [react(), tailwindcss(), chainOpsApiPlugin()],
    resolve: {
      alias: {
        '@': path.resolve(process.cwd(), '.'),
      },
    },
    server: {
      hmr: process.env.DISABLE_HMR !== 'true',
      watch: process.env.DISABLE_HMR === 'true' ? null : {},
    },
  };
});

