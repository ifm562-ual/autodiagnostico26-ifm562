# ---------- Build Stage ----------
FROM node:24-alpine AS build

WORKDIR /app

# Install Angular CLI
RUN npm install -g @angular/cli@21

# Install dependencies
COPY package*.json ./
RUN npm ci

# Copy source
COPY . .

# Build production bundle
RUN ng build --configuration production

# ---------- Runtime Stage ----------
FROM nginx:alpine

# Remove default nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Copy Angular build output
COPY --from=build /app/dist/autodiagnostico/browser /usr/share/nginx/html

# Expose port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

