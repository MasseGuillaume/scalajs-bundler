module.exports = require('./scalajs.webpack.config');
// Expose the Scala.js artifact as a library in the `sjs` namespace
module.exports.output.library = 'sjs';
