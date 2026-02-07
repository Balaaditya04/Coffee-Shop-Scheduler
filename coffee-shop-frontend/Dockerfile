FROM node:18-slim
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
# Using a simple static server like 'serve' or just 'npm start' as per user request
# User requested CMD ["npm","start"]
CMD ["npm","start"]
EXPOSE 3000
