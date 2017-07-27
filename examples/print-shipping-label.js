// Print a shipping label
// Required features: script input, binary HttpAction request/response with automatic content types.

var http = system.import('rockscript.io/http');

var customer = http.get({
  url: 'http://api.example.com/customers/' + script.input.customerId
});

var barcode = http.post({
  url: 'http://api.example.com/barcodes',
  body: {
    ean: script.input.product.ean
  }
});

var labelPdf = http.post({
  url: 'http://api.example.com/shipping-labels',
  body: {
    address: customer.body.address,
    barcode: barcode.body.url
  }
});

http.post({
  url: 'http://api.example.com/print-jobs',
  body: labelPdf
});