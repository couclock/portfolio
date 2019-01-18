import axios from 'axios';

console.log('VUE_APP_BASE_URI : ', process.env.VUE_APP_BASE_URI);

export const baseURL = process.env.VUE_APP_BASE_URI ? process.env.VUE_APP_BASE_URI : 'http://localhost:8080/';

export const HTTP = axios.create({
  baseURL: baseURL
});
