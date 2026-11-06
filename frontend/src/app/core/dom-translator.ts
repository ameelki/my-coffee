import { I18n } from './i18n.service';

const ATTRS = ['placeholder', 'title', 'aria-label', 'alt'];
const SKIP = new Set(['SCRIPT', 'STYLE', 'TEXTAREA', 'CODE', 'TITLE']);

/**
 * Traduit l'interface à la volée : remplace chaque texte français (et placeholder / title / aria-label)
 * par sa traduction, y compris pour les éléments ajoutés plus tard par Angular.
 * Ne fait rien en français.
 */
export function startDomTranslation(i18n: I18n): void {
  document.documentElement.lang = ({ lb: 'lb', pt: 'pt-PT', de: 'de-LU', fr: 'fr' } as Record<string, string>)[i18n.lang];
  if (i18n.lang === 'fr') return;

  // confirm() : messages traduits aussi
  const nativeConfirm = window.confirm.bind(window);
  window.confirm = (message?: string) => nativeConfirm(i18n.tr(message ?? ''));

  const shownText = new WeakMap<Node, string>();
  const shownAttr = new WeakMap<Element, Record<string, string>>();

  const translateText = (n: Node): void => {
    const parent = n.parentElement;
    if (!parent || SKIP.has(parent.tagName)) return;
    const t = n.nodeValue ?? '';
    if (shownText.get(n) === t) return;
    const trimmed = t.trim();
    if (!trimmed) return;
    const out = i18n.tr(trimmed);
    if (out !== trimmed.replace(/\s+/g, ' ')) {
      const nv = t.replace(trimmed, () => out);
      shownText.set(n, nv);
      n.nodeValue = nv;
    }
  };

  const translateAttrs = (el: Element): void => {
    for (const a of ATTRS) {
      const v = el.getAttribute(a);
      if (!v) continue;
      const seen = shownAttr.get(el) ?? {};
      if (seen[a] === v) continue;
      const out = i18n.tr(v);
      if (out !== v) {
        seen[a] = out;
        shownAttr.set(el, seen);
        el.setAttribute(a, out);
      }
    }
  };

  const walk = (root: Node): void => {
    if (root.nodeType === Node.TEXT_NODE) { translateText(root); return; }
    if (root.nodeType !== Node.ELEMENT_NODE) return;
    const el = root as Element;
    if (SKIP.has(el.tagName)) return;
    translateAttrs(el);
    el.childNodes.forEach(walk);
  };

  new MutationObserver((mutations) => {
    for (const m of mutations) {
      if (m.type === 'childList') m.addedNodes.forEach(walk);
      else if (m.type === 'characterData') translateText(m.target);
      else if (m.type === 'attributes') translateAttrs(m.target as Element);
    }
  }).observe(document.body, { subtree: true, childList: true, characterData: true, attributes: true, attributeFilter: ATTRS });

  walk(document.body);
}
