const linkRegExp = /<link rel="stylesheet" href="(.+)">/;

function getAsyncLink(href) {
  return `
    <link rel="preload" href="${href}" as="style" onload="this.onload=null;this.rel='stylesheet'">
    <noscript><link rel="stylesheet" href="${href}"></noscript>
    `;
}

function getHref(indexHtml) {
  const matchs = indexHtml.match(linkRegExp);
  return (matchs && matchs.length>1) ? matchs[1] : null;
}

module.exports = (_, indexHtml) => {
  const headClosingTagIdx = indexHtml.indexOf('</head>');

  const headPart = indexHtml.slice(0, headClosingTagIdx);
  const href = getHref(indexHtml);
  if(!href)
    return indexHtml;
    
  const asyncLinkPart = getAsyncLink(getHref(indexHtml));
  const bodyPart = indexHtml.slice(headClosingTagIdx);

  return `
  ${headPart.replace(linkRegExp, '')}
  ${asyncLinkPart}
  ${bodyPart}
  `;
};