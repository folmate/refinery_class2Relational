/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import type {
  CompletionContext,
  CompletionResult,
} from '@codemirror/autocomplete';
import type { Transaction } from '@codemirror/state';

import type PWAStore from '../PWAStore';
import type EditorStore from '../editor/EditorStore';
import getLogger from '../utils/getLogger';

import ContentAssistService from './ContentAssistService';
import HighlightingService from './HighlightingService';
import OccurrencesService from './OccurrencesService';
import SemanticsService from './SemanticsService';
import UpdateService from './UpdateService';
import ValidationService from './ValidationService';
import XtextWebSocketClient from './XtextWebSocketClient';
import type { XtextWebPushService } from './xtextMessages';

const log = getLogger('xtext.XtextClient');

export default class XtextClient {
  readonly webSocketClient: XtextWebSocketClient;

  private readonly updateService: UpdateService;

  private readonly contentAssistService: ContentAssistService;

  private readonly highlightingService: HighlightingService;

  private readonly validationService: ValidationService;

  private readonly occurrencesService: OccurrencesService;

  private readonly semanticsService: SemanticsService;

  constructor(
    private readonly store: EditorStore,
    private readonly pwaStore: PWAStore,
  ) {
    this.webSocketClient = new XtextWebSocketClient(
      () => this.onReconnect(),
      () => this.onDisconnect(),
      this.onPush.bind(this),
    );
    this.updateService = new UpdateService(store, this.webSocketClient);
    this.contentAssistService = new ContentAssistService(this.updateService);
    this.highlightingService = new HighlightingService(
      store,
      this.updateService,
    );
    this.validationService = new ValidationService(store, this.updateService);
    this.occurrencesService = new OccurrencesService(store, this.updateService);
    this.semanticsService = new SemanticsService(store, this.validationService);
  }

  start(): void {
    this.webSocketClient.start();
  }

  private onReconnect(): void {
    this.updateService.onReconnect();
    this.occurrencesService.onReconnect();
    this.pwaStore.checkForUpdates();
  }

  private onDisconnect(): void {
    this.store.analysisCompleted(true);
    this.highlightingService.onDisconnect();
    this.validationService.onDisconnect();
    this.occurrencesService.onDisconnect();
  }

  onTransaction(transaction: Transaction): void {
    // `ContentAssistService.prototype.onTransaction` needs the dirty change desc
    // _before_ the current edit, so we call it before `updateService`.
    this.contentAssistService.onTransaction(transaction);
    this.updateService.onTransaction(transaction);
    this.occurrencesService.onTransaction(transaction);
  }

  private onPush(
    resource: string,
    stateId: string,
    service: XtextWebPushService,
    push: unknown,
  ) {
    const { resourceName, xtextStateId } = this.updateService;
    if (resource !== resourceName) {
      log.error(
        'Unknown resource name: expected:',
        resourceName,
        'got:',
        resource,
      );
      return;
    }
    if (stateId !== xtextStateId) {
      log.error(
        'Unexpected xtext state id: expected:',
        xtextStateId,
        'got:',
        stateId,
      );
      // The current push message might be stale (referring to a previous state),
      // so this is not neccessarily an error and there is no need to force-reconnect.
      return;
    }
    switch (service) {
      case 'highlight':
        this.highlightingService.onPush(push);
        return;
      case 'validate':
        this.validationService.onPush(push);
        return;
      case 'semantics':
        this.semanticsService.onPush(push);
        return;
      default:
        throw new Error('Unknown service');
    }
  }

  contentAssist(context: CompletionContext): Promise<CompletionResult> {
    return this.contentAssistService.contentAssist(context);
  }

  formatText(): void {
    this.updateService.formatText().catch((e) => {
      log.error('Error while formatting text', e);
    });
  }

  dispose(): void {
    this.webSocketClient.disconnect();
  }
}
