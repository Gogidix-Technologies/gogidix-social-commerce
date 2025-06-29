# Invoice Service Documentation

## Overview

The Invoice Service is a comprehensive financial document management system within the Social E-commerce Ecosystem that handles automated invoice generation, multi-currency billing, tax calculations, and compliance management. This Spring Boot service provides enterprise-grade invoicing capabilities for B2B transactions, vendor payments, commission settlements, and customer billing across multiple regions and currencies.

## Business Context

In a global social commerce ecosystem serving multiple regions (Europe and Africa) with diverse vendors, customers, and payment structures, comprehensive invoicing is essential for:

- **Financial Compliance**: Meeting regulatory requirements for invoice generation across different jurisdictions
- **Multi-Currency Operations**: Supporting transactions in EUR, USD, GBP, MAD, EGP, NGN, and other regional currencies
- **Vendor Management**: Automated invoicing for vendor commission settlements and marketplace fees
- **B2B Transactions**: Professional invoicing for enterprise customers and bulk orders
- **Tax Compliance**: Accurate VAT, GST, and regional tax calculations and reporting
- **Audit Trail**: Complete financial audit trail for all transactions and settlements
- **Revenue Recognition**: Proper revenue recognition for accounting and financial reporting
- **Multi-Language Support**: Invoices in multiple languages (EN, FR, DE, ES, AR) for regional compliance

The Invoice Service ensures financial transparency, regulatory compliance, and professional billing operations across the entire social commerce platform.

## Current Implementation Status

### ✅ Implemented Features
- **Service Infrastructure**: Spring Boot 3.1.5 application with Eureka service discovery
- **Health Monitoring**: Actuator endpoints for service health and metrics
- **Database Integration**: PostgreSQL and H2 database support for invoice storage
- **Basic REST API**: Health check endpoints and service initialization
- **Multi-Language Support**: Internationalization structure for EN, FR, DE, ES, AR
- **Development Environment**: Complete development and testing setup

### 🚧 In Development
- **Invoice Generation Engine**: Automated invoice creation for orders, commissions, and settlements
- **Multi-Currency Support**: Real-time currency conversion and multi-currency invoicing
- **Tax Calculation Engine**: Automated tax calculations for different regions and products
- **Template System**: Customizable invoice templates for different business scenarios
- **PDF Generation**: Professional PDF invoice generation with branding

### 📋 Planned Features
- **Advanced Tax Management**: Complex tax scenarios including reverse charge and cross-border VAT
- **Electronic Invoice Integration**: EDI and electronic invoice standards compliance
- **Automated Reconciliation**: Automatic matching of payments with invoices
- **Revenue Analytics**: Comprehensive revenue reporting and analytics
- **Integration APIs**: Deep integration with accounting systems like SAP, QuickBooks, Xero
- **AI-Powered Insights**: Intelligent invoice analysis and payment prediction

## Components

### Core Components

- **InvoiceServiceApplication**: Main Spring Boot application with service discovery
- **Invoice Generation Engine**: Automated invoice creation and management
- **Multi-Currency Processor**: Currency conversion and multi-currency billing
- **Tax Calculation Engine**: Automated tax calculations and compliance
- **Template Manager**: Invoice template management and customization

### Invoice Management Components

- **Invoice Builder**: Dynamic invoice creation with flexible line items
- **Invoice Validator**: Comprehensive validation for accuracy and compliance
- **Invoice Formatter**: Professional formatting for different invoice types
- **Invoice Scheduler**: Automated recurring invoice generation
- **Invoice Tracker**: Real-time invoice status tracking and updates

### Financial Components

- **Currency Converter**: Real-time currency conversion with rate management
- **Tax Calculator**: Multi-jurisdiction tax calculation and reporting
- **Commission Calculator**: Automated commission and fee calculations
- **Payment Tracker**: Integration with payment services for invoice settlement
- **Revenue Recognizer**: Proper revenue recognition for accounting compliance

### Document Generation Components

- **PDF Generator**: Professional PDF invoice generation with branding
- **Template Engine**: Customizable invoice templates for different scenarios
- **Document Storage**: Secure storage and retrieval of invoice documents
- **Digital Signature**: Electronic signature support for legal compliance
- **Document Versioning**: Version control for invoice modifications

### Integration Components

- **Order Integration**: Seamless integration with order management system
- **Payment Integration**: Real-time integration with payment processing services
- **Commission Integration**: Automated commission settlement invoicing
- **Accounting Integration**: Export capabilities for external accounting systems
- **Notification Integration**: Automated invoice delivery and reminders

### Data Access Layer

- **Invoice Repository**: Invoice data storage and retrieval
- **Invoice Line Item Repository**: Detailed line item management
- **Tax Rate Repository**: Tax rate storage and historical tracking
- **Currency Rate Repository**: Exchange rate management and history
- **Template Repository**: Invoice template storage and versioning

### Utility Services

- **Number Generator**: Sequential invoice numbering with customizable formats
- **Date Calculator**: Payment terms and due date calculations
- **Validation Service**: Comprehensive data validation and business rule enforcement
- **Audit Logger**: Complete audit trail for all invoice operations
- **Configuration Manager**: Dynamic configuration for tax rates and business rules

### External Integration Components

- **Currency Exchange Service**: Real-time exchange rate updates
- **Tax Service Integration**: Integration with external tax calculation services
- **Email Service**: Professional invoice delivery via email
- **Document Service**: Integration with document management systems
- **Analytics Service**: Invoice analytics and reporting integration

## Getting Started

### Prerequisites
- Java 17 or higher
- PostgreSQL database (for production) or H2 (for development)
- Currency exchange rate service access
- Email service configuration (SMTP or cloud email service)
- PDF generation libraries (Apache PDFBox or iText)
- Tax rate data sources

### Quick Start
1. Configure database connection for invoice storage
2. Set up currency exchange rate service integration
3. Configure email service for invoice delivery
4. Set up tax rate data sources for compliance
5. Run `mvn spring-boot:run` to start the service
6. Access API documentation at `http://localhost:8106/swagger-ui.html`

### Basic Configuration Example

```yaml
# application.yml
server:
  port: 8106

spring:
  application:
    name: invoice-service
  datasource:
    url: jdbc:postgresql://localhost:5432/invoice_db
    username: invoice_user
    password: invoice_password
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

invoice:
  numbering:
    prefix: "INV"
    format: "INV-{YYYY}-{MM}-{####}"
    starting-number: 1000
  currency:
    default: EUR
    supported: [EUR, USD, GBP, MAD, EGP, NGN]
    rate-service-url: http://currency-exchange:3001
  tax:
    default-rate: 0.20
    regions:
      EU: 0.20
      UK: 0.20
      MA: 0.20
      EG: 0.14
      NG: 0.075
  templates:
    default: standard-invoice
    b2b: business-invoice
    commission: commission-settlement
  pdf:
    engine: apache-pdfbox
    fonts-path: /fonts
    logo-path: /images/logo.png

email:
  smtp:
    host: smtp.gmail.com
    port: 587
    username: invoices@social-commerce.com
    password: ${EMAIL_PASSWORD}
```

## Examples

### Invoice Generation API

```bash
# Generate invoice for an order
curl -X POST http://localhost:8106/api/v1/invoices/generate \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "ORDER_INVOICE",
    "orderId": "ORDER_12345",
    "customerId": "CUST_98765",
    "currency": "EUR",
    "items": [
      {
        "description": "Premium Social Commerce Package",
        "quantity": 1,
        "unitPrice": 299.99,
        "taxRate": 0.20
      }
    ],
    "billingAddress": {
      "name": "Tech Solutions GmbH",
      "street": "Hauptstraße 123",
      "city": "Berlin",
      "postalCode": "10115",
      "country": "DE"
    }
  }'

# Generate commission settlement invoice
curl -X POST http://localhost:8106/api/v1/invoices/commission \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "vendorId": "VENDOR_456",
    "period": "2024-01",
    "currency": "EUR",
    "commissions": [
      {
        "orderId": "ORDER_111",
        "amount": 45.50,
        "rate": 0.15
      },
      {
        "orderId": "ORDER_112", 
        "amount": 67.25,
        "rate": 0.15
      }
    ]
  }'
```

### Multi-Currency Invoice Generation

```java
// Example: Multi-currency invoice generation service
@Service
public class MultiCurrencyInvoiceService {
    
    @Autowired
    private CurrencyExchangeService currencyService;
    
    @Autowired
    private TaxCalculationService taxService;
    
    @Autowired
    private InvoiceTemplateService templateService;
    
    public Invoice generateMultiCurrencyInvoice(InvoiceRequest request) {
        // Get current exchange rates
        ExchangeRates rates = currencyService.getCurrentRates();
        
        // Build invoice with multi-currency support
        Invoice invoice = Invoice.builder()
            .number(generateInvoiceNumber())
            .date(LocalDate.now())
            .dueDate(calculateDueDate(request.getPaymentTerms()))
            .currency(request.getCurrency())
            .customer(buildCustomerInfo(request.getCustomerId()))
            .build();
        
        // Process line items with currency conversion
        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceLineItemRequest itemRequest : request.getItems()) {
            InvoiceLineItem item = processLineItem(itemRequest, request.getCurrency(), rates);
            invoice.addLineItem(item);
            subtotal = subtotal.add(item.getTotal());
        }
        
        // Calculate taxes based on region and currency
        TaxCalculation taxCalc = taxService.calculateTax(
            subtotal, 
            request.getCurrency(),
            request.getBillingAddress().getCountry()
        );
        
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxCalc.getTotalTax());
        invoice.setTotal(subtotal.add(taxCalc.getTotalTax()));
        
        // Apply currency-specific formatting
        formatInvoiceAmounts(invoice, request.getCurrency());
        
        return invoiceRepository.save(invoice);
    }
    
    private InvoiceLineItem processLineItem(InvoiceLineItemRequest request, 
                                          String targetCurrency, 
                                          ExchangeRates rates) {
        // Convert price if needed
        BigDecimal unitPrice = request.getUnitPrice();
        if (!request.getCurrency().equals(targetCurrency)) {
            unitPrice = currencyService.convert(
                unitPrice, 
                request.getCurrency(), 
                targetCurrency, 
                rates
            );
        }
        
        BigDecimal lineTotal = unitPrice.multiply(new BigDecimal(request.getQuantity()));
        
        return InvoiceLineItem.builder()
            .description(request.getDescription())
            .quantity(request.getQuantity())
            .unitPrice(unitPrice)
            .total(lineTotal)
            .taxRate(request.getTaxRate())
            .currency(targetCurrency)
            .build();
    }
}
```

### Automated Tax Calculation

```java
// Example: Multi-jurisdiction tax calculation
@Service
public class TaxCalculationService {
    
    @Autowired
    private TaxRateRepository taxRateRepository;
    
    public TaxCalculation calculateTax(BigDecimal amount, String currency, String country) {
        TaxCalculation calculation = new TaxCalculation();
        
        // Get applicable tax rates for country
        List<TaxRate> applicableRates = taxRateRepository.findByCountryAndCurrency(country, currency);
        
        for (TaxRate rate : applicableRates) {
            TaxComponent component = new TaxComponent();
            component.setType(rate.getTaxType());
            component.setRate(rate.getRate());
            component.setAmount(amount.multiply(rate.getRate()));
            
            calculation.addComponent(component);
        }
        
        // Handle special cases
        if (isEUCountry(country) && isB2BTransaction()) {
            // Apply reverse charge mechanism
            calculation = applyReverseCharge(calculation);
        }
        
        if (isDigitalService() && isEUCountry(country)) {
            // Apply digital services VAT rules
            calculation = applyDigitalServicesVAT(calculation, country);
        }
        
        return calculation;
    }
    
    private TaxCalculation applyReverseCharge(TaxCalculation calculation) {
        // Reverse charge: customer pays VAT instead of supplier
        calculation.setReverseCharge(true);
        calculation.getComponents().forEach(component -> {
            if (component.getType() == TaxType.VAT) {
                component.setReverseCharge(true);
                component.setAmount(BigDecimal.ZERO);
            }
        });
        return calculation;
    }
}
```

### PDF Invoice Generation

```java
// Example: Professional PDF invoice generation
@Service
public class PDFInvoiceGenerationService {
    
    @Autowired
    private InvoiceTemplateService templateService;
    
    public byte[] generatePDFInvoice(Invoice invoice) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDDocument document = new PDDocument();
            
            // Load company branding
            PDPageContentStream contentStream = setupPage(document);
            
            // Add company logo and header
            addCompanyHeader(contentStream, invoice);
            
            // Add customer information
            addCustomerInformation(contentStream, invoice);
            
            // Add invoice details
            addInvoiceDetails(contentStream, invoice);
            
            // Add line items table
            addLineItemsTable(contentStream, invoice);
            
            // Add totals section
            addTotalsSection(contentStream, invoice);
            
            // Add payment terms and footer
            addPaymentTermsAndFooter(contentStream, invoice);
            
            contentStream.close();
            document.save(output);
            document.close();
            
            return output.toByteArray();
            
        } catch (IOException e) {
            throw new InvoiceGenerationException("Failed to generate PDF invoice", e);
        }
    }
    
    private void addLineItemsTable(PDPageContentStream contentStream, Invoice invoice) 
            throws IOException {
        
        float yPosition = 500f;
        float tableWidth = 500f;
        float rowHeight = 20f;
        
        // Table headers
        String[] headers = {"Description", "Qty", "Unit Price", "Total"};
        drawTableHeaders(contentStream, headers, yPosition, tableWidth);
        
        yPosition -= rowHeight;
        
        // Line items
        for (InvoiceLineItem item : invoice.getLineItems()) {
            String[] rowData = {
                item.getDescription(),
                String.valueOf(item.getQuantity()),
                formatCurrency(item.getUnitPrice(), invoice.getCurrency()),
                formatCurrency(item.getTotal(), invoice.getCurrency())
            };
            
            drawTableRow(contentStream, rowData, yPosition, tableWidth);
            yPosition -= rowHeight;
        }
        
        // Draw table borders
        drawTableBorders(contentStream, yPosition + rowHeight, tableWidth, 
                        invoice.getLineItems().size() + 1);
    }
    
    private String formatCurrency(BigDecimal amount, String currency) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        formatter.setCurrency(Currency.getInstance(currency));
        return formatter.format(amount);
    }
}
```

## Best Practices

### Invoice Generation
1. **Sequential Numbering**: Implement tamper-proof sequential invoice numbering for audit compliance
2. **Validation Rules**: Comprehensive validation to ensure invoice accuracy and compliance
3. **Template Management**: Flexible template system for different invoice types and regions
4. **Currency Handling**: Accurate currency conversion with proper rounding and precision
5. **Date Management**: Proper handling of invoice dates, due dates, and payment terms

### Tax Compliance
1. **Multi-Jurisdiction Support**: Accurate tax calculations for different countries and regions
2. **Tax Rate Management**: Regular updates to tax rates and compliance rules
3. **Special Scenarios**: Handle reverse charge, digital services VAT, and cross-border transactions
4. **Audit Trail**: Complete audit trail for all tax calculations and adjustments
5. **Compliance Reporting**: Automated generation of tax reports for regulatory submission

### Financial Accuracy
1. **Precision Handling**: Use BigDecimal for all financial calculations to avoid rounding errors
2. **Currency Conversion**: Real-time exchange rates with fallback to cached rates
3. **Reconciliation**: Automated reconciliation between invoices and payments
4. **Error Handling**: Comprehensive error handling for financial operations
5. **Data Integrity**: Strong data validation and integrity constraints

### Document Management
1. **Version Control**: Maintain version history for invoice modifications
2. **Digital Signatures**: Electronic signatures for legal compliance where required
3. **Secure Storage**: Encrypted storage for sensitive financial documents
4. **Backup and Recovery**: Reliable backup and disaster recovery procedures
5. **Access Control**: Role-based access control for invoice operations

### Integration
1. **API Design**: RESTful APIs with clear versioning and backward compatibility
2. **Event-Driven**: Use events for invoice lifecycle notifications
3. **Idempotency**: Ensure API operations are idempotent for reliability
4. **Rate Limiting**: Implement rate limiting for external integrations
5. **Error Recovery**: Robust error recovery and retry mechanisms

## Invoice Types and Scenarios

### Order Invoices
- **Customer Orders**: Standard invoices for customer purchases
- **B2B Invoices**: Professional invoices for business customers
- **Subscription Invoices**: Recurring invoices for subscription services
- **Proforma Invoices**: Preliminary invoices for quotes and estimates

### Settlement Invoices
- **Commission Settlements**: Automated vendor commission settlements
- **Marketplace Fees**: Platform fees and transaction charges
- **Advertising Fees**: Social media advertising and promotion charges
- **Service Fees**: Additional service charges and premium features

### Compliance Scenarios
- **Cross-Border Transactions**: International invoicing with proper tax treatment
- **Digital Services**: VAT compliance for digital products and services
- **Reverse Charge**: B2B transactions within EU with reverse charge mechanism
- **Multi-Currency**: Invoices in different currencies with proper conversion

### Regional Adaptations
- **European Union**: VAT compliance and GDPR considerations
- **United Kingdom**: Post-Brexit VAT and trade compliance
- **Africa**: Regional tax compliance and currency support
- **Multi-Language**: Localized invoices in regional languages

## Development Roadmap

### Phase 1: Core Foundation (🚧)
- 🚧 Invoice generation engine for orders and commissions
- 🚧 Multi-currency support with real-time conversion
- 🚧 Basic tax calculation for major regions
- 🚧 PDF generation with professional templates
- 📋 Email delivery and customer notifications

### Phase 2: Advanced Features (📋)
- 📋 Complex tax scenarios and compliance rules
- 📋 Electronic invoice standards (UBL, EDI)
- 📋 Automated reconciliation with payments
- 📋 Advanced template customization
- 📋 Revenue recognition automation

### Phase 3: Enterprise Integration (📋)
- 📋 Accounting system integrations (SAP, QuickBooks)
- 📋 Advanced analytics and reporting
- 📋 Automated compliance reporting
- 📋 AI-powered invoice insights
- 📋 Mobile invoice management

### Phase 4: Global Scale (📋)
- 📋 Global tax compliance automation
- 📋 Real-time financial reporting
- 📋 Advanced fraud detection
- 📋 Blockchain-based invoice verification
- 📋 Predictive analytics for cash flow