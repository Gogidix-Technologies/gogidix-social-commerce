// Admin Framework Integration: data/regions.ts
// Social Commerce regions for admin-framework integration

import { Region } from 'admin-framework';

/**
 * Social Commerce specific regions for the Global HQ Admin Dashboard
 */
export const socialCommerceRegions: Region[] = [
  {
    id: 'na',
    name: 'North America',
    code: 'NA',
    description: 'North American operations for social commerce',
    status: 'ACTIVE',
    properties: {
      currency: 'USD',
      timezone: 'America/New_York',
      languages: ['en-US', 'es-MX', 'fr-CA'],
      marketplaces: ['usa', 'canada', 'mexico']
    }
  },
  {
    id: 'eu',
    name: 'Europe',
    code: 'EU',
    description: 'European operations for social commerce',
    status: 'ACTIVE',
    properties: {
      currency: 'EUR',
      timezone: 'Europe/Paris',
      languages: ['en-GB', 'de-DE', 'fr-FR', 'es-ES', 'it-IT'],
      marketplaces: ['uk', 'germany', 'france', 'spain', 'italy']
    }
  },
  {
    id: 'apac',
    name: 'Asia Pacific',
    code: 'APAC',
    description: 'Asia Pacific operations for social commerce',
    status: 'ACTIVE',
    properties: {
      currency: 'USD',
      timezone: 'Asia/Singapore',
      languages: ['en-SG', 'zh-CN', 'ja-JP', 'ko-KR'],
      marketplaces: ['singapore', 'australia', 'japan', 'korea']
    }
  },
  {
    id: 'latam',
    name: 'Latin America',
    code: 'LATAM',
    description: 'Latin American operations for social commerce',
    status: 'ACTIVE',
    properties: {
      currency: 'USD',
      timezone: 'America/Sao_Paulo',
      languages: ['es-419', 'pt-BR'],
      marketplaces: ['brazil', 'argentina', 'colombia', 'chile']
    }
  },
  {
    id: 'mena',
    name: 'Middle East & North Africa',
    code: 'MENA',
    description: 'Middle East and North Africa operations for social commerce',
    status: 'ACTIVE',
    properties: {
      currency: 'USD',
      timezone: 'Asia/Dubai',
      languages: ['ar', 'en'],
      marketplaces: ['uae', 'saudi', 'egypt']
    }
  }
];

export default socialCommerceRegions;
